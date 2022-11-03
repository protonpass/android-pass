package me.proton.android.pass.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PassPreferences {
    val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    val THEME = intPreferencesKey("theme")
}
