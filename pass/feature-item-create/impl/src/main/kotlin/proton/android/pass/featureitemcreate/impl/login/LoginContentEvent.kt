package proton.android.pass.featureitemcreate.impl.login

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldEvent
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

sealed interface LoginContentEvent {
    object Up : LoginContentEvent
    data class Success(
        val shareId: ShareId,
        val itemId: ItemId,
        val model: ItemUiModel
    ) : LoginContentEvent
    data class Submit(val shareId: ShareId) : LoginContentEvent
    data class OnUsernameChange(val username: String) : LoginContentEvent
    data class OnPasswordChange(val password: String) : LoginContentEvent
    data class OnWebsiteEvent(val event: WebsiteSectionEvent) : LoginContentEvent
    data class OnNoteChange(val note: String) : LoginContentEvent
    data class OnTotpChange(val totp: String) : LoginContentEvent
    object PasteTotp : LoginContentEvent
    data class OnLinkedAppDelete(val app: PackageInfoUi) : LoginContentEvent
    data class OnCustomFieldEvent(val event: CustomFieldEvent) : LoginContentEvent
    data class OnFocusChange(val field: LoginField, val isFocused: Boolean) : LoginContentEvent
}
