package me.proton.android.pass.preferences

sealed interface ThemePreference {
    object Light : ThemePreference
    object Dark : ThemePreference
    object System : ThemePreference

    companion object
}
