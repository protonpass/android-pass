package me.proton.pass.presentation.uievents

sealed interface IsButtonEnabled {
    object Enabled : IsButtonEnabled
    object Disabled : IsButtonEnabled
}
