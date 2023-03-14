package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object EditNote : NavItem(
    baseRoute = "note/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.updateNoteGraph(
    onNoteUpdateSuccess: (ShareId, ItemId) -> Unit,
    onBackClick: () -> Unit,
) {
    composable(EditNote) {
        UpdateNote(
            onUpClick = onBackClick,
            onSuccess = onNoteUpdateSuccess
        )
    }
}
