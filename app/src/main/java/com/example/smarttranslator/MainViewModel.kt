package com.example.smarttranslator

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface TextTranslatorState {
    data object Preparing : TextTranslatorState
    data object DownloadingModel : TextTranslatorState
    data object Ready : TextTranslatorState
    data object Translating : TextTranslatorState
    data class Success(val text: String, val targetLang: String) : TextTranslatorState
    data class Error(
        val source: String,
        val target: String,
        val message: String,
        val isDownloadFailure: Boolean = false
    ) : TextTranslatorState
}

sealed interface PhraseTranslatorState {
    val phrase: String
    val targetLang: String

    data class Preparing(
        override val phrase: String,
        override val targetLang: String
    ) : PhraseTranslatorState

    data class DownloadingModel(
        override val phrase: String,
        override val targetLang: String
    ) : PhraseTranslatorState

    data class Translating(
        override val phrase: String,
        override val targetLang: String
    ) : PhraseTranslatorState

    data class Success(
        override val phrase: String,
        override val targetLang: String,
        val text: String
    ) : PhraseTranslatorState

    data class Error(
        override val phrase: String,
        override val targetLang: String,
        val message: String
    ) : PhraseTranslatorState
}

private const val DOWNLOAD_TIMEOUT_MS = 60_000L
private const val TRANSLATE_TIMEOUT_MS = 30_000L

private const val OFFLINE_MESSAGE =
    "No internet connection. Connect once to download this language, then tap Retry."

private class OfflineException : Exception()

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val visualTargetLang: StateFlow<String?> = Settings.visualTargetFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateVisualTarget(code: String) {
        viewModelScope.launch { Settings.saveVisual(context, code) }
    }

    val textLanguages: StateFlow<TextLanguages?> = Settings.textLanguagesFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateTextLanguages(source: String, target: String) {
        viewModelScope.launch { Settings.saveText(context, source, target) }
    }

    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _textState =
        MutableStateFlow<TextTranslatorState>(TextTranslatorState.Preparing)
    val textState: StateFlow<TextTranslatorState> = _textState.asStateFlow()

    private var textTranslator: Translator? = null
    private var textTranslatorKey: String? = null
    private var prepareTextJob: Job? = null
    private var translateTextJob: Job? = null

    fun onSourceTextChanged(text: String) {
        _sourceText.value = text

        translateTextJob?.cancel()

        when (val current = _textState.value) {
            is TextTranslatorState.Translating,
            is TextTranslatorState.Success -> {
                _textState.value = TextTranslatorState.Ready
            }
            is TextTranslatorState.Error -> {
                if (!current.isDownloadFailure) {
                    _textState.value = TextTranslatorState.Ready
                }
            }
            else -> Unit
        }
    }

    fun prepareTextTranslator(source: String, target: String) {
        val key = "$source-$target"

        if (textTranslatorKey == key) {
            if (textTranslator != null) return
            if (_textState.value is TextTranslatorState.Preparing) return
            if (_textState.value is TextTranslatorState.DownloadingModel) return
        }

        prepareTextJob?.cancel()
        translateTextJob?.cancel()

        textTranslator?.close()
        textTranslator = null
        textTranslatorKey = key
        _textState.value = TextTranslatorState.Preparing

        prepareTextJob = viewModelScope.launch {
            val client = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build()
            )

            try {
                withTimeout(DOWNLOAD_TIMEOUT_MS.milliseconds) {
                    if (needsDownload(source, target)) {
                        if (!isOnline()) throw OfflineException()
                        _textState.value = TextTranslatorState.DownloadingModel
                    }
                    client.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
                }
                if (textTranslatorKey == key) {
                    textTranslator = client
                    _textState.value = TextTranslatorState.Ready
                } else {
                    client.close()
                }
            } catch (_: TimeoutCancellationException) {
                client.close()
                if (textTranslatorKey == key) {
                    textTranslator = null
                    _textState.value = TextTranslatorState.Error(
                        source = source,
                        target = target,
                        message = "The model download timed out. Check your connection and tap Retry.",
                        isDownloadFailure = true
                    )
                }
            } catch (e: CancellationException) {
                client.close()
                throw e
            } catch (e: Exception) {
                client.close()
                if (textTranslatorKey == key) {
                    textTranslator = null
                    _textState.value = TextTranslatorState.Error(
                        source = source,
                        target = target,
                        message = if (e is OfflineException) {
                            OFFLINE_MESSAGE
                        } else {
                            "Couldn't download the translation model. Check your connection and tap Retry."
                        },
                        isDownloadFailure = true
                    )
                }
            }
        }
    }

    fun retryTextTranslator() {
        val current = _textState.value
        if (current is TextTranslatorState.Error) {
            textTranslatorKey = null
            prepareTextTranslator(current.source, current.target)
        }
    }

    fun translateText(source: String, target: String, text: String) {
        val translator = textTranslator ?: return
        if (textTranslatorKey != "$source-$target") return
        if (text.isBlank()) return
        if (_textState.value is TextTranslatorState.Translating) return

        _textState.value = TextTranslatorState.Translating

        translateTextJob = viewModelScope.launch {
            try {
                val result = withTimeout(TRANSLATE_TIMEOUT_MS.milliseconds) {
                    translator.translate(text).await()
                }
                _textState.value = TextTranslatorState.Success(result, target)
            } catch (_: TimeoutCancellationException) {
                _textState.value = TextTranslatorState.Error(
                    source = source,
                    target = target,
                    message = "Translation timed out. Tap Translate to try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _textState.value = TextTranslatorState.Error(
                    source = source,
                    target = target,
                    message = "Couldn't translate the text. Tap Translate to try again."
                )
            }
        }
    }

    private val _phraseState = MutableStateFlow<PhraseTranslatorState?>(null)
    val phraseState: StateFlow<PhraseTranslatorState?> = _phraseState.asStateFlow()

    private var phraseTranslator: Translator? = null
    private var phraseTranslatorTarget: String? = null
    private var phraseJob: Job? = null

    fun translatePhrase(phrase: String, target: String) {
        val current = _phraseState.value

        if (current != null && current.phrase == phrase && current.targetLang == target) {
            if (current is PhraseTranslatorState.Success) return
            if (current is PhraseTranslatorState.Preparing) return
            if (current is PhraseTranslatorState.DownloadingModel) return
            if (current is PhraseTranslatorState.Translating) return
        }

        phraseJob?.cancel()
        _phraseState.value = PhraseTranslatorState.Preparing(phrase, target)

        val needsNewClient = phraseTranslatorTarget != target || phraseTranslator == null
        if (needsNewClient) {
            phraseTranslator?.close()
            phraseTranslator = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(target)
                    .build()
            )
            phraseTranslatorTarget = target
        }

        val translator = phraseTranslator ?: run {
            _phraseState.value = PhraseTranslatorState.Error(
                phrase = phrase,
                targetLang = target,
                message = "Translator unavailable."
            )
            return
        }

        phraseJob = viewModelScope.launch {
            var modelReady = false
            try {
                withTimeout(DOWNLOAD_TIMEOUT_MS.milliseconds) {
                    if (needsDownload(TranslateLanguage.ENGLISH, target)) {
                        if (!isOnline()) throw OfflineException()
                        _phraseState.value =
                            PhraseTranslatorState.DownloadingModel(phrase, target)
                    }
                    translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
                }
                modelReady = true

                _phraseState.value = PhraseTranslatorState.Translating(phrase, target)

                val result = withTimeout(TRANSLATE_TIMEOUT_MS.milliseconds) {
                    translator.translate(phrase).await()
                }
                _phraseState.value = PhraseTranslatorState.Success(phrase, target, result)
            } catch (_: TimeoutCancellationException) {
                if (phraseTranslatorTarget == target) {
                    phraseTranslator?.close()
                    phraseTranslator = null
                    phraseTranslatorTarget = null
                }
                _phraseState.value = PhraseTranslatorState.Error(
                    phrase = phrase,
                    targetLang = target,
                    message = if (modelReady) {
                        "Translation timed out. Tap Retry."
                    } else {
                        "The model download timed out. Check your connection and tap Retry."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (phraseTranslatorTarget == target) {
                    phraseTranslator?.close()
                    phraseTranslator = null
                    phraseTranslatorTarget = null
                }
                _phraseState.value = PhraseTranslatorState.Error(
                    phrase = phrase,
                    targetLang = target,
                    message = if (e is OfflineException) {
                        OFFLINE_MESSAGE
                    } else if (modelReady) {
                        "Couldn't translate the phrase. Tap Retry."
                    } else {
                        "Couldn't download the translation model. Check your connection and tap Retry."
                    }
                )
            }
        }
    }

    fun retryPhrase() {
        val current = _phraseState.value
        if (current is PhraseTranslatorState.Error) {
            _phraseState.value = null
            translatePhrase(current.phrase, current.targetLang)
        }
    }

    private suspend fun needsDownload(vararg languages: String): Boolean {
        val downloaded = RemoteModelManager.getInstance()
            .getDownloadedModels(TranslateRemoteModel::class.java)
            .await()
            .map { it.language }
        return languages.any { it != TranslateLanguage.ENGLISH && it !in downloaded }
    }

    private fun isOnline(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun onCleared() {
        textTranslator?.close()
        phraseTranslator?.close()
    }
}