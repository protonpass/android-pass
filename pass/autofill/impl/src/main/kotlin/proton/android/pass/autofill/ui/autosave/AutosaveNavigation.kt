package proton.android.pass.autofill.ui.autosave

sealed interface AutosaveNavigation {
    object Success : AutosaveNavigation
    object Cancel : AutosaveNavigation
    object Upgrade : AutosaveNavigation
}
