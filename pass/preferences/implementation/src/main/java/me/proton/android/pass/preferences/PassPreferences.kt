package me.proton.android.pass.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PassPreferences {
    val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    val HAS_AUTHENTICATED = booleanPreferencesKey("has_authenticated")
    val HAS_COMPLETED_ON_BOARDING = booleanPreferencesKey("has_completed_on_boarding")
    val THEME = intPreferencesKey("theme")
}
