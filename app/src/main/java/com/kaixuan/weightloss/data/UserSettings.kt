package com.kaixuan.weightloss.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class WeightUnit(val label: String, val factor: Float) {
    KG("kg", 1f),
    JIN("斤", 2f)
}

data class UserSettings(
    val targetWeight: Float = 0f,
    val height: Float = 0f,         // cm
    val weightUnit: WeightUnit = WeightUnit.KG,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)

class UserSettingsRepository(private val context: Context) {

    private object Keys {
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val HEIGHT = floatPreferencesKey("height")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            targetWeight = prefs[Keys.TARGET_WEIGHT] ?: 0f,
            height = prefs[Keys.HEIGHT] ?: 0f,
            weightUnit = WeightUnit.valueOf(prefs[Keys.WEIGHT_UNIT] ?: WeightUnit.KG.name),
            reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 8,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun updateTargetWeight(weight: Float) {
        context.dataStore.edit { it[Keys.TARGET_WEIGHT] = weight }
    }

    suspend fun updateHeight(height: Float) {
        context.dataStore.edit { it[Keys.HEIGHT] = height }
    }

    suspend fun updateWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { it[Keys.WEIGHT_UNIT] = unit.name }
    }

    suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }
}
