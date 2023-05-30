package proton.android.pass.featureitemcreate.impl.login

import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldType
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

sealed interface BaseLoginNavigation {
    data class LoginCreated(val itemUiModel: ItemUiModel) : BaseLoginNavigation
    data class LoginUpdated(val shareId: ShareId, val itemId: ItemId) : BaseLoginNavigation
    data class CreateAlias(
        val shareId: ShareId,
        val showUpgrade: Boolean,
        val title: Option<String>
    ) : BaseLoginNavigation

    object GeneratePassword : BaseLoginNavigation
    object Upgrade : BaseLoginNavigation
    object ScanTotp : BaseLoginNavigation
    object Close : BaseLoginNavigation

    data class AliasOptions(
        val shareId: ShareId,
        val showUpgrade: Boolean,
    ) : BaseLoginNavigation
    object DeleteAlias : BaseLoginNavigation
    data class EditAlias(
        val shareId: ShareId,
        val showUpgrade: Boolean
    ) : BaseLoginNavigation

    data class SelectVault(
        val shareId: ShareId
    ) : BaseLoginNavigation

    object AddCustomField : BaseLoginNavigation
    data class CustomFieldTypeSelected(
        val type: CustomFieldType
    ) : BaseLoginNavigation

    data class CustomFieldOptions(val currentValue: String, val index: Int) : BaseLoginNavigation
    data class EditCustomField(val currentValue: String, val index: Int) : BaseLoginNavigation
    object RemovedCustomField : BaseLoginNavigation
}
