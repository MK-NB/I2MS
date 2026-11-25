package com.example.smarttranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import dagger.hilt.android.AndroidEntryPoint

private fun localizedText(text: String, language: LanguageOption) = buildAnnotatedString {
    withStyle(SpanStyle(localeList = LocaleList(language.localeTag))) {
        append(text)
    }
}

private val appTypography = Typography().let {
    it.copy(labelLarge = it.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp))
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(typography = appTypography) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by rememberSaveable { mutableStateOf("home") }
    var selectedCategoryTitle by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCategory = remember(selectedCategoryTitle) {
        visualCategories.find { it.title == selectedCategoryTitle }
    }

    BackHandler(enabled = currentScreen != "home") {
        currentScreen = if (currentScreen == "visual_phrases") "visual_categories" else "home"
    }

    AnimatedVisibility(visible = currentScreen == "home", enter = fadeIn(), exit = fadeOut()) {
        HomeScreen(
            onTextClick = { currentScreen = "text_translator" },
            onVisualClick = { currentScreen = "visual_categories" }
        )
    }

    AnimatedVisibility(visible = currentScreen == "text_translator", enter = fadeIn(), exit = fadeOut()) {
        TextTranslatorScreen(onBackClick = { currentScreen = "home" })
    }

    AnimatedVisibility(visible = currentScreen == "visual_categories", enter = fadeIn(), exit = fadeOut()) {
        VisualCategoriesScreen(
            onBackClick = { currentScreen = "home" },
            onCategorySelected = { category ->
                selectedCategoryTitle = category.title
                currentScreen = "visual_phrases"
            }
        )
    }

    AnimatedVisibility(visible = currentScreen == "visual_phrases", enter = fadeIn(), exit = fadeOut()) {
        if (selectedCategory != null) {
            VisualPhrasesScreen(
                category = selectedCategory,
                onBackClick = { currentScreen = "visual_categories" }
            )
        }
    }
}

@Composable
private fun BackButton(
    destination: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Go back to $destination",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun HomeScreen(onTextClick: () -> Unit, onVisualClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SMART TRANSLATOR",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(48.dp))

                HomeActionButton(
                    icon = Icons.Default.GridView,
                    title = "Quick phrases",
                    subtitle = "Speak using simple symbols",
                    onClick = onVisualClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                HomeActionButton(
                    icon = Icons.Default.Edit,
                    title = "Text translator",
                    subtitle = "Enter your own text",
                    onClick = onTextClick
                )
            }
        }
    }
}

@Composable
private fun HomeActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                shape = shape
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp,
            focusedElevation = 5.dp,
            hoveredElevation = 5.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TextTranslatorScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val savedLanguages by viewModel.textLanguages.collectAsState()
    val languages = savedLanguages ?: return
    val sourceLanguage = remember(languages.source) {
        languageOf(languages.source)
    }
    val targetLanguage = remember(languages.target) {
        languageOf(languages.target, fallback = TranslateLanguage.POLISH)
    }
    val state by viewModel.textState.collectAsState()
    val sourceText by viewModel.sourceText.collectAsState()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(sourceText, TextRange(sourceText.length)))
    }

    LaunchedEffect(sourceLanguage.code, targetLanguage.code) {
        viewModel.prepareTextTranslator(sourceLanguage.code, targetLanguage.code)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackButton(destination = "menu", onClick = onBackClick)

        Text(
            text = "Text Translator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LanguageDropdown(
                    label = "Source language",
                    selectedLanguage = sourceLanguage,
                    onLanguageSelected = { newSource ->
                        val newTarget = if (newSource.code == languages.target) {
                            languages.source
                        } else {
                            languages.target
                        }
                        viewModel.updateTextLanguages(newSource.code, newTarget)
                    },
                    modifier = Modifier.weight(1f),
                    fillWidth = true
                )

                IconButton(
                    onClick = {
                        viewModel.updateTextLanguages(languages.target, languages.source)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap languages"
                    )
                }

                LanguageDropdown(
                    label = "Target language",
                    selectedLanguage = targetLanguage,
                    onLanguageSelected = { newTarget ->
                        val newSource = if (newTarget.code == languages.source) {
                            languages.target
                        } else {
                            languages.source
                        }
                        viewModel.updateTextLanguages(newSource, newTarget.code)
                    },
                    modifier = Modifier.weight(1f),
                    fillWidth = true
                )
            }
        }

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val textChanged = newValue.text != textFieldValue.text
                textFieldValue = newValue
                if (textChanged) viewModel.onSourceTextChanged(newValue.text)
            },
            label = { Text("Enter text (in ${sourceLanguage.name})") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
        )

        when (val s = state) {
            is TextTranslatorState.Preparing ->
                StatusRow("Preparing translator…")
            is TextTranslatorState.DownloadingModel ->
                StatusRow("Downloading translation model…")
            is TextTranslatorState.Translating ->
                StatusRow("Translating…")
            is TextTranslatorState.Error ->
                if (s.isDownloadFailure) {
                    ErrorStatusRow(
                        message = s.message,
                        onRetry = { viewModel.retryTextTranslator() }
                    )
                }
            else -> Unit
        }

        Button(
            onClick = {
                viewModel.translateText(
                    sourceLanguage.code,
                    targetLanguage.code,
                    sourceText
                )
            },
            enabled = sourceText.isNotBlank() &&
                    state !is TextTranslatorState.Preparing &&
                    state !is TextTranslatorState.DownloadingModel &&
                    state !is TextTranslatorState.Translating &&
                    (state as? TextTranslatorState.Error)?.isDownloadFailure != true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
            )
        ) {
            Text("Translate")
        }

        Text(
            text = "Translation:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )

        TextResultCard(
            state = state,
            onRetry = {
                viewModel.translateText(
                    sourceLanguage.code,
                    targetLanguage.code,
                    sourceText
                )
            }
        )
    }
}

@Composable
private fun TextResultCard(
    state: TextTranslatorState,
    onRetry: () -> Unit
) {
    val translationError =
        (state as? TextTranslatorState.Error)?.takeUnless { it.isDownloadFailure }
    val content: AnnotatedString = when {
        state is TextTranslatorState.Success -> localizedText(state.text, languageOf(state.targetLang))
        translationError != null -> AnnotatedString(translationError.message)
        else -> AnnotatedString("Your translation will appear here")
    }
    val announce = state is TextTranslatorState.Success || translationError != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.titleLarge,
                color = if (translationError != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                modifier = Modifier.semantics {
                    if (announce) liveRegion = LiveRegionMode.Polite
                }
            )
            if (translationError != null) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun StatusRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorStatusRow(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        TextButton(
            onClick = onRetry,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun LanguageDropdown(
    label: String,
    selectedLanguage: LanguageOption?,
    onLanguageSelected: (LanguageOption) -> Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
    prefix: String = ""
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val name = selectedLanguage?.name ?: "…"

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = selectedLanguage != null,
            contentPadding = PaddingValues(horizontal = if (fillWidth) 8.dp else 16.dp),
            modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .defaultMinSize(minHeight = 48.dp)
                .semantics { contentDescription = "$label: $name" }
        ) {
            Box(
                Modifier
                    .weight(1f, fill = fillWidth)
                    .clearAndSetSemantics { }
            ) {
                Text("$prefix$name")
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            supportedLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    },
                    modifier = Modifier.semantics {
                        selected = language.code == selectedLanguage?.code
                    }
                )
            }
        }
    }
}

@Composable
fun VisualCategoriesScreen(
    onBackClick: () -> Unit,
    onCategorySelected: (VisualCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(destination = "menu", onClick = onBackClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose a category:",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .semantics { heading() }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(visualCategories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .clickable(
                            onClickLabel = "Open category",
                            onClick = { onCategorySelected(category) }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = category.title,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualPhrasesScreen(
    category: VisualCategory,
    onBackClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val savedLanguage by viewModel.visualTargetLang.collectAsState()
    val selectedTargetLanguage = remember(savedLanguage) {
        savedLanguage?.let { languageOf(it) }
    }

    var activePhraseIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val activePhrase = remember(activePhraseIndex, category) {
        activePhraseIndex?.let { category.visualPhrases.getOrNull(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackButton(destination = "categories", onClick = onBackClick)

            LanguageDropdown(
                label = "Target language",
                selectedLanguage = selectedTargetLanguage,
                onLanguageSelected = { viewModel.updateVisualTarget(it.code) },
                prefix = "Language: "
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(category.visualPhrases) { index, phrase ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .clickable(
                            onClickLabel = "View translation",
                            onClick = { activePhraseIndex = index }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            phrase.symbolIcons.forEach { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = phrase.phrase,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp
                            ),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    if (activePhrase != null && selectedTargetLanguage != null) {
        VisualTranslationDialog(
            phrase = activePhrase,
            targetLanguage = selectedTargetLanguage,
            onDismiss = { activePhraseIndex = null },
            viewModel = viewModel
        )
    }
}

@Composable
fun VisualTranslationDialog(
    phrase: VisualPhrase,
    targetLanguage: LanguageOption,
    onDismiss: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var optionIndex by rememberSaveable(phrase.phrase) { mutableStateOf(0) }
    var numberInput by rememberSaveable(phrase.phrase) { mutableStateOf("") }
    var number by rememberSaveable(phrase.phrase) { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val confirmNumber = {
        number = numberInput
        focusManager.clearFocus()
    }

    val option = phrase.choices?.options?.getOrNull(optionIndex)
    val sentence = when {
        phrase.choices != null && option != null ->
            phrase.choices.template.replace("{x}", option.label)
        phrase.number != null && number.isNotEmpty() ->
            phrase.number.template.replace("{n}", number)
        else -> phrase.phrase
    }
    val symbols = phrase.symbolIcons.mapIndexed { index, icon ->
        if (option != null && index == phrase.choices?.iconIndex) option.icon else icon
    }

    val latestState by viewModel.phraseState.collectAsState()
    val state = latestState?.takeIf {
        it.phrase == sentence && it.targetLang == targetLanguage.code
    }

    LaunchedEffect(sentence, targetLanguage.code) {
        viewModel.translatePhrase(sentence, targetLanguage.code)
    }

    val message: AnnotatedString = when (state) {
        is PhraseTranslatorState.Success -> localizedText(state.text, targetLanguage)
        is PhraseTranslatorState.Error -> AnnotatedString(state.message)
        is PhraseTranslatorState.Preparing -> AnnotatedString("Preparing translator…")
        is PhraseTranslatorState.DownloadingModel -> AnnotatedString("Downloading model…")
        is PhraseTranslatorState.Translating -> AnnotatedString("Translating…")
        null -> AnnotatedString("")
    }
    val isSuccess = state is PhraseTranslatorState.Success

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Close")
            }
        },
        title = {
            Text(text = "Picture Translator", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    symbols.forEach { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = localizedText(sentence, languageOf(TranslateLanguage.ENGLISH)),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                phrase.choices?.let { choices ->
                    FlowRow(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp, Alignment.CenterHorizontally
                        )
                    ) {
                        choices.options.forEachIndexed { index, choice ->
                            FilterChip(
                                selected = index == optionIndex,
                                onClick = { optionIndex = index },
                                label = { Text(choice.label) },
                                modifier = Modifier.semantics { role = Role.RadioButton }
                            )
                        }
                    }
                }

                phrase.number?.let { slot ->
                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { input ->
                            numberInput = input.filter { it.isLetterOrDigit() }.take(6)
                        },
                        label = { Text(slot.label) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { confirmNumber() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { confirmNumber() },
                        enabled = numberInput != number
                    ) {
                        Text("Update translation")
                    }
                }

                if (state is PhraseTranslatorState.Preparing ||
                    state is PhraseTranslatorState.DownloadingModel ||
                    state is PhraseTranslatorState.Translating
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }

                Surface(
                    color = if (isSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        style = if (isSuccess) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        fontWeight = if (isSuccess) FontWeight.Bold else null,
                        textAlign = TextAlign.Center,
                        color = when (state) {
                            is PhraseTranslatorState.Success ->
                                MaterialTheme.colorScheme.onPrimaryContainer
                            is PhraseTranslatorState.Error ->
                                MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .semantics {
                                if (state is PhraseTranslatorState.Success ||
                                    state is PhraseTranslatorState.Error ||
                                    state is PhraseTranslatorState.DownloadingModel
                                ) {
                                    liveRegion = LiveRegionMode.Polite
                                }
                            }
                    )
                }

                if (state is PhraseTranslatorState.Error) {
                    TextButton(
                        onClick = { viewModel.retryPhrase() },
                        modifier = Modifier.defaultMinSize(
                            minWidth = 48.dp,
                            minHeight = 48.dp
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    )
}