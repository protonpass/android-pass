package me.proton.pass.presentation.create.login

internal interface OnWebsiteChange {
    val onWebsiteValueChanged: (String, Int) -> Unit
    val onAddWebsite: () -> Unit
    val onRemoveWebsite: (Int) -> Unit
}
