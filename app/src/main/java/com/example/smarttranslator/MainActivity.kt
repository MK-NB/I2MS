package com.example.smarttranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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

data class LanguageOption(val name: String, val code: String)

val supportedLanguages = listOf(
    LanguageOption("Czech", TranslateLanguage.CZECH),
    LanguageOption("English", TranslateLanguage.ENGLISH),
    LanguageOption("French", TranslateLanguage.FRENCH),
    LanguageOption("German", TranslateLanguage.GERMAN),
    LanguageOption("Italian", TranslateLanguage.ITALIAN),
    LanguageOption("Polish", TranslateLanguage.POLISH),
    LanguageOption("Portuguese", TranslateLanguage.PORTUGUESE),
    LanguageOption("Russian", TranslateLanguage.RUSSIAN),
    LanguageOption("Spanish", TranslateLanguage.SPANISH),
    LanguageOption("Ukrainian", TranslateLanguage.UKRAINIAN)
)

@Composable
fun AppNavigation() {
    var currentScreen by rememberSaveable { mutableStateOf("home") }

    AnimatedVisibility(visible = currentScreen == "home", enter = fadeIn(), exit = fadeOut()) {
        HomeScreen(
            onTextClick = { currentScreen = "text_translator" },
            onVisualClick = { currentScreen = "visual_categories" }
        )
    }

    AnimatedVisibility(visible = currentScreen == "text_translator", enter = fadeIn(), exit = fadeOut()) {
        TextTranslatorScreen(
            onBackClick = { currentScreen = "home" }
        )
    }
}

@Composable
fun HomeScreen(onTextClick: () -> Unit, onVisualClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SMART TRANSLATOR",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onTextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Column {
                        Text(text = "Text translator", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Enter your own text", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onVisualClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Column {
                        Text(text = "Picture translator", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Speak using simple symbols", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun TextTranslatorScreen(
    onBackClick: () -> Unit,
) {
    var sourceLanguage by remember { mutableStateOf(supportedLanguages[1]) }
    var targetLanguage by remember { mutableStateOf(supportedLanguages[5]) }

    var sourceText by rememberSaveable { mutableStateOf("") }
    var translatedText by rememberSaveable { mutableStateOf("") }

    var isDownloadingModel by rememberSaveable { mutableStateOf(false) }
    var currentTranslator by rememberSaveable { mutableStateOf<Translator?>(null) }

    LaunchedEffect(sourceLanguage, targetLanguage) {
        currentTranslator?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage.code)
            .setTargetLanguage(targetLanguage.code)
            .build()

        val client = Translation.getClient(options)
        currentTranslator = client

        isDownloadingModel = true

        val conditions = DownloadConditions.Builder().build()
        client.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isDownloadingModel = false
                translatedText = ""
            }
            .addOnFailureListener { e ->
                isDownloadingModel = false
                translatedText = "An unexpected error occurred: ${e.message}"
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentTranslator?.close()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("◄ Back to menu")
        }

        Text(
            text = "Text Translator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextDropdown(
                    selectedLanguage = sourceLanguage,
                    onLanguageSelected = { newSource ->
                        if (newSource == targetLanguage) {
                            targetLanguage = sourceLanguage
                        }
                        sourceLanguage = newSource
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val temp = sourceLanguage
                        sourceLanguage = targetLanguage
                        targetLanguage = temp

                    }
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                }

                TextDropdown(
                    selectedLanguage = targetLanguage,
                    onLanguageSelected = { newTarget ->
                        if (newTarget == sourceLanguage) {
                            sourceLanguage = targetLanguage
                        }
                        targetLanguage = newTarget
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            label = {
                Text("Enter text (in ${sourceLanguage.name})")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        )

        Button(
            onClick = {
                if (sourceText.isNotBlank() && currentTranslator != null) {
                    currentTranslator?.translate(sourceText)
                        ?.addOnSuccessListener { result ->
                            translatedText = result
                        }
                        ?.addOnFailureListener { e ->
                            translatedText = "An unexpected error occurred: ${e.message}"
                        }
                }
            },
            enabled = !isDownloadingModel && sourceText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Translate")
        }

        Text(
            text = "Translation:",
            fontSize = 18.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = translatedText,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun TextDropdown(
    selectedLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedLanguage.name,
                modifier = Modifier.weight(1f)
            )
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
                    }
                )
            }
        }
    }
}