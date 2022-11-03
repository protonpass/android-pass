package me.proton.android.pass.preferences.extensions

import me.proton.android.pass.preferences.ThemePreference

private const val THEME_SYSTEM = 1
private const val THEME_LIGHT = 2
private const val THEME_DARK = 3

fun ThemePreference.value(): Int =
    when (this) {
        ThemePreference.System -> THEME_SYSTEM
        ThemePreference.Light -> THEME_LIGHT
        ThemePreference.Dark -> THEME_DARK
    }

fun ThemePreference.Companion.from(value: Int): ThemePreference =
    when (value) {
        THEME_SYSTEM -> ThemePreference.System
        THEME_LIGHT -> ThemePreference.Light
        THEME_DARK -> ThemePreference.Dark
        else -> ThemePreference.System
    }


