package me.proton.android.pass.preferences.extensions

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val PREFERENCES_NAME = "pass_preferences"

val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)
