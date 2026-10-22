package com.example.smarttranslator

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Settings {
    private val VISUAL_LANG = stringPreferencesKey("visual_lang")
    private val TEXT_SOURCE = stringPreferencesKey("text_source")
    private val TEXT_TARGET = stringPreferencesKey("text_target")

    suspend fun saveVisual(context: Context, lang: String) {
        context.dataStore.edit { it[VISUAL_LANG] = lang }
    }

    suspend fun saveText(context: Context, source: String, target: String) {
        context.dataStore.edit {
            it[TEXT_SOURCE] = source
            it[TEXT_TARGET] = target
        }
    }

    fun visualTargetFlow(context: Context): Flow<String> = context.dataStore.data
        .map { it[VISUAL_LANG] ?: TranslateLanguage.ENGLISH }

    fun textSourceFlow(context: Context): Flow<String> = context.dataStore.data
        .map { it[TEXT_SOURCE] ?: TranslateLanguage.ENGLISH }

    fun textTargetFlow(context: Context): Flow<String> = context.dataStore.data
        .map { it[TEXT_TARGET] ?: TranslateLanguage.POLISH }
}
