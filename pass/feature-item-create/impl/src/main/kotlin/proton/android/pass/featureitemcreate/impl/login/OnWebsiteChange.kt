package proton.android.pass.featureitemcreate.impl.login

internal interface OnWebsiteChange {
    val onWebsiteValueChanged: (String, Int) -> Unit
    val onAddWebsite: () -> Unit
    val onRemoveWebsite: (Int) -> Unit
}
