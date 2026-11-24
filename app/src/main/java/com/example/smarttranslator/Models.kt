package com.example.smarttranslator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.mlkit.nl.translate.TranslateLanguage

data class LanguageOption(val name: String, val code: String, val localeTag: String)

val supportedLanguages = listOf(
    LanguageOption("Czech", TranslateLanguage.CZECH, "cs-CZ"),
    LanguageOption("English", TranslateLanguage.ENGLISH, "en-US"),
    LanguageOption("French", TranslateLanguage.FRENCH, "fr-FR"),
    LanguageOption("German", TranslateLanguage.GERMAN, "de-DE"),
    LanguageOption("Italian", TranslateLanguage.ITALIAN, "it-IT"),
    LanguageOption("Polish", TranslateLanguage.POLISH, "pl-PL"),
    LanguageOption("Portuguese", TranslateLanguage.PORTUGUESE, "pt-PT"),
    LanguageOption("Russian", TranslateLanguage.RUSSIAN, "ru-RU"),
    LanguageOption("Spanish", TranslateLanguage.SPANISH, "es-ES"),
    LanguageOption("Ukrainian", TranslateLanguage.UKRAINIAN, "uk-UA")
)

private val languagesByCode = supportedLanguages.associateBy { it.code }

fun languageOf(code: String, fallback: String = TranslateLanguage.ENGLISH): LanguageOption =
    languagesByCode[code] ?: languagesByCode.getValue(fallback)

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
                symbolIcons = listOf(
                    Icons.Default.DirectionsBus,
                    Icons.AutoMirrored.Filled.ArrowForward,
                    Icons.Default.Schedule
                ),
                phrase = "What time does the next bus leave?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.ConfirmationNumber,
                    Icons.AutoMirrored.Filled.Help,
                    Icons.Default.AttachMoney
                ),
                phrase = "How much is the ticket?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    Icons.AutoMirrored.Filled.ArrowForward,
                    Icons.Default.Train
                ),
                phrase = "Where is the nearest station?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.DirectionsBus,
                    Icons.AutoMirrored.Filled.Help,
                    Icons.Default.Schedule
                ),
                phrase = "Does this bus go to the city center?"
            )
        )
    ),
    VisualCategory(
        title = "Restaurant / Food",
        icon = Icons.Default.Restaurant,
        visualPhrases = listOf(
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.AutoMirrored.Filled.MenuBook,
                    Icons.Default.Restaurant,
                    Icons.Default.PanTool
                ),
                phrase = "Could I have the menu, please?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.LocalBar,
                    Icons.Default.WaterDrop,
                    Icons.Default.PanTool
                ),
                phrase = "I'd like still water, please."
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Receipt,
                    Icons.Default.CreditCard,
                    Icons.Default.ThumbUp
                ),
                phrase = "Can I pay the bill?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Restaurant,
                    Icons.AutoMirrored.Filled.Help,
                    Icons.Default.ThumbUp
                ),
                phrase = "Do you have vegetarian options?"
            )
        )
    ),
    VisualCategory(
        title = "Hotel / Accommodation",
        icon = Icons.Default.Hotel,
        visualPhrases = listOf(
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Wifi,
                    Icons.Default.Key,
                    Icons.AutoMirrored.Filled.Help
                ),
                phrase = "What is the Wi-Fi password?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Key,
                    Icons.Default.Bed,
                    Icons.Default.PanTool
                ),
                phrase = "Can I have my room key?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Hotel,
                    Icons.Default.Schedule,
                    Icons.Default.PanTool
                ),
                phrase = "What time is check-out?"
            )
        )
    ),
    VisualCategory(
        title = "Help / Emergency",
        icon = Icons.Default.LocalHospital,
        visualPhrases = listOf(
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.Warning,
                    Icons.Default.MedicalServices,
                    Icons.Default.LocalHospital
                ),
                phrase = "I need a doctor!"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    Icons.AutoMirrored.Filled.ArrowForward,
                    Icons.Default.Wc
                ),
                phrase = "Where is the restroom?"
            ),
            VisualPhrase(
                symbolIcons = listOf(
                    Icons.Default.MedicalServices,
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    Icons.AutoMirrored.Filled.ArrowForward
                ),
                phrase = "Where is the nearest pharmacy?"
            )
        )
    )
)