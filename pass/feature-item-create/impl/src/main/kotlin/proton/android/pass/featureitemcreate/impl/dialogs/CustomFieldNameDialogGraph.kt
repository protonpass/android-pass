package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.dialog

object CustomFieldTypeNavArgId : NavArgId {
    override val key = "customFieldType"
    override val navType = NavType.StringType
}

object CustomFieldNameDialog : NavItem(
    baseRoute = "item/create/customfield/add/dialog",
    navArgIds = listOf(CustomFieldTypeNavArgId)
) {
    fun buildRoute(type: CustomFieldType) = "$baseRoute/${type.name}"
}

sealed interface CustomFieldNameNavigation {
    object Close : CustomFieldNameNavigation
}

fun NavGraphBuilder.customFieldNameDialogGraph(
    onNavigate: (CustomFieldNameNavigation) -> Unit
) {
    dialog(CustomFieldNameDialog) {
        CustomFieldNameDialog(onNavigate = onNavigate)
    }
}
