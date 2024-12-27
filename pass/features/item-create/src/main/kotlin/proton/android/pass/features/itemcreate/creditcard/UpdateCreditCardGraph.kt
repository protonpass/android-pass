package proton.android.pass.features.itemcreate.creditcard

import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object EditCreditCard : NavItem(
    baseRoute = "creditcard/edit/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface UpdateCreditCardNavigation : BaseCreditCardNavigation {
    data class ItemUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateCreditCardNavigation
}

fun NavGraphBuilder.updateCreditCardGraph(onNavigate: (BaseCreditCardNavigation) -> Unit) {
    composable(EditCreditCard) {
        UpdateCreditCardScreen(
            onNavigate = onNavigate
        )
    }
}

