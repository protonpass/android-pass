package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object CustomFieldIndexNavArgId : NavArgId {
    override val key = "index"
    override val navType = NavType.IntType
}

object AddCustomFieldBottomSheet : NavItem("item/create/customfield/add/bottomsheet")
object CustomFieldOptionsBottomSheet : NavItem(
    baseRoute = "item/create/customfield/options/bottomsheet",
    navArgIds = listOf(CustomFieldIndexNavArgId)
) {
    fun buildRoute(index: Int) = "$baseRoute/$index"
}

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

    object EditCustomField : CustomFieldNavigation
    object RemoveCustomField : CustomFieldNavigation
}

fun NavGraphBuilder.customFieldBottomSheetGraph(
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

                else -> {}
            }
        }
    }

    bottomSheet(CustomFieldOptionsBottomSheet) { navStack ->
        val index = navStack.arguments?.getInt(CustomFieldIndexNavArgId.key)
            ?: throw IllegalStateException("Index is required")
        EditCustomFieldBottomSheet(
            index = index,
            onNavigate = {
                when (it) {
                    CustomFieldNavigation.EditCustomField -> {
                        onNavigate(BaseLoginNavigation.EditCustomField(index))
                    }
                    CustomFieldNavigation.RemoveCustomField -> {
                        onNavigate(BaseLoginNavigation.RemovedCustomField)
                    }

                    else -> {}
                }
            }
        )
    }
}
