package proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.note.UpdateNote
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.EditNote
import proton.android.pass.ui.navigation.Home
import proton.android.pass.ui.navigation.ViewItem

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.updateNoteGraph(nav: AppNavigator) {
    composable(EditNote) {
        UpdateNote(
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(shareId, itemId),
                    backDestination = Home
                )
            }
        )
    }
}
