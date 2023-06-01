package proton.android.pass.featureitemdetail.impl.login

import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldEvent

sealed interface LoginDetailEvent {
    object OnTogglePasswordClick : LoginDetailEvent
    object OnUsernameClick : LoginDetailEvent
    object OnGoToAliasClick : LoginDetailEvent
    object OnCopyPasswordClick : LoginDetailEvent
    data class OnWebsiteClicked(val website: String) : LoginDetailEvent
    data class OnWebsiteLongClicked(val website: String) : LoginDetailEvent
    data class OnCopyTotpClick(val totpCode: String) : LoginDetailEvent
    object OnUpgradeClick : LoginDetailEvent

    data class OnCustomFieldEvent(val event: CustomFieldEvent) : LoginDetailEvent
}
