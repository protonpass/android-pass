package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

sealed interface AutofillItemOptionsNavigation {
    object Close : AutofillItemOptionsNavigation
}

object AutofillItemOptionsBottomSheet : NavItem(
    baseRoute = "autofill/itemoptions/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

fun NavGraphBuilder.autofillItemOptionsGraph(
    onNavigate: (AutofillItemOptionsNavigation) -> Unit
) {
    bottomSheet(AutofillItemOptionsBottomSheet) {
        BackHandler {
            onNavigate(AutofillItemOptionsNavigation.Close)
        }
        AutofillItemOptionsBottomSheet(
            onNavigate = onNavigate
        )
    }
}
