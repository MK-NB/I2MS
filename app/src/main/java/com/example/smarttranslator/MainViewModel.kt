package com.example.smarttranslator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val visualTargetLang: StateFlow<String> = Settings.visualTargetFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, TranslateLanguage.ENGLISH)

    fun updateVisualTarget(code: String) {
        viewModelScope.launch {
            Settings.saveVisual(context, code)
        }
    }

    val textSourceLang: StateFlow<String> = Settings.textSourceFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, TranslateLanguage.ENGLISH)

    val textTargetLang: StateFlow<String> = Settings.textTargetFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, TranslateLanguage.POLISH)

    fun updateTextLanguages(source: String, target: String) {
        viewModelScope.launch {
            Settings.saveText(context, source, target)
        }
    }
}