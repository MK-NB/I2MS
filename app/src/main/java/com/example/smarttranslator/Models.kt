package com.example.smarttranslator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.mlkit.nl.translate.TranslateLanguage

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

data class VisualPhrase(
    val symbolIcons: List<ImageVector>,
    val phrase: String
)

data class VisualCategory(
    val title: String,
    val icon: ImageVector,
    val visualPhrases: List<VisualPhrase>
)

val visualCategories = listOf(
    VisualCategory(
        title = "Bus Stop / Transport",
        icon = Icons.Default.DirectionsBus,
        visualPhrases = listOf(
            VisualPhrase(
                symbolIcons = listOf(Icons.Default.DirectionsBus,
                    Icons.AutoMirrored.Filled.ArrowForward,
                    Icons.Default.Schedule),
                phrase = "What time does the next bus leave?"
            ),
            VisualPhrase(
                symbolIcons = listOf(Icons.Default.ConfirmationNumber,
                    Icons.AutoMirrored.Filled.Help,
                    Icons.Default.AttachMoney),
                phrase = "How much is the ticket?"
            )
        )
    ),
    VisualCategory(
        title = "Restaurant / Food",
        icon = Icons.Default.Restaurant,
        visualPhrases = listOf(
            VisualPhrase(
                symbolIcons = listOf(Icons.AutoMirrored.Filled.MenuBook,
                    Icons.Default.Restaurant,
                    Icons.Default.PanTool),
                phrase = "Could I have the menu, please?"
            ),
            VisualPhrase(
                symbolIcons = listOf(Icons.Default.Receipt,
                    Icons.Default.CreditCard,
                    Icons.Default.ThumbUp),
                phrase = "Can I pay the bill?"
            )
        )
    )
)