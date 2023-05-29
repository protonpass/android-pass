package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object AddCustomFieldBottomSheet : NavItem("item/create/customfield/add/bottomsheet")

enum class CustomFieldType {
    Text,
    Hidden,
    Totp
}

sealed interface CustomFieldNavigation {
    object Close : CustomFieldNavigation
    object AddText : CustomFieldNavigation
    object AddHidden : CustomFieldNavigation
    object AddTotp : CustomFieldNavigation
}

fun NavGraphBuilder.addCustomFieldBottomSheetGraph(
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    bottomSheet(AddCustomFieldBottomSheet) {
        AddCustomFieldBottomSheet {
            when (it) {
                is CustomFieldNavigation.Close -> {
                    onNavigate(BaseLoginNavigation.Close)
                }
                is CustomFieldNavigation.AddText -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Text))
                }
                is CustomFieldNavigation.AddHidden -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Hidden))
                }
                is CustomFieldNavigation.AddTotp -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Totp))
                }
            }
        }
    }
}
