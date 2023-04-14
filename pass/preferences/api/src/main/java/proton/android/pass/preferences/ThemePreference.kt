package proton.android.pass.preferences

private const val THEME_SYSTEM = 1
private const val THEME_LIGHT = 2
private const val THEME_DARK = 3

enum class ThemePreference {
    Light,
    Dark,
    System;

    fun value(): Int =
        when (this) {
            System -> THEME_SYSTEM
            Light -> THEME_LIGHT
            Dark -> THEME_DARK
        }

    companion object {
        fun from(value: Int): ThemePreference =
            when (value) {
                THEME_SYSTEM -> System
                THEME_LIGHT -> Light
                THEME_DARK -> Dark
                else -> Dark
            }
    }
}
