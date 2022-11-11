package me.proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.create.note.CreateNote

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createNoteGraph(nav: AppNavigator) {
    composable(AppNavItem.CreateNote) {
        CreateNote(
            onUpClick = { nav.onBackClick() },
            onSuccess = { nav.onBackClick() }
        )
    }
}
