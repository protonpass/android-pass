package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

object EditCreditCard : NavItem(
    baseRoute = "creditcard/edit/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface UpdateCreditCardNavigation : BaseCreditCardNavigation {
    data class ItemUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateCreditCardNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.updateCreditCardGraph(
    onNavigate: (BaseCreditCardNavigation) -> Unit,
) {
    composable(EditCreditCard) {
        UpdateCreditCardScreen(
            onNavigate = onNavigate
        )
    }
}

