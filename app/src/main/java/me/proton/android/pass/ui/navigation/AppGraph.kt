package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.core.pass.presentation.create.alias.createAliasGraph
import me.proton.core.pass.presentation.create.alias.updateAliasGraph
import me.proton.core.pass.presentation.create.login.createLoginGraph
import me.proton.core.pass.presentation.create.login.updateLoginGraph
import me.proton.core.pass.presentation.create.note.createNoteGraph
import me.proton.core.pass.presentation.create.note.updateNoteGraph
import me.proton.core.pass.presentation.create.password.createPasswordGraph
import me.proton.android.pass.ui.detail.itemDetailGraph
import me.proton.android.pass.ui.help.helpGraph
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.home.homeGraph
import me.proton.android.pass.ui.settings.settingsGraph
import me.proton.android.pass.ui.trash.trashGraph

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.appGraph(
    appNavigation: AppNavigator,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    homeGraph(
        navigationDrawer,
        HomeScreenNavigation(appNavigation),
        onDrawerIconClick
    )
    trashGraph(navigationDrawer, onDrawerIconClick)
    helpGraph(navigationDrawer, onDrawerIconClick)
    settingsGraph(navigationDrawer, onDrawerIconClick)
    createLoginGraph(appNavigation)
    updateLoginGraph(appNavigation)
    createNoteGraph(appNavigation)
    updateNoteGraph(appNavigation)
    createAliasGraph(appNavigation)
    updateAliasGraph(appNavigation)
    createPasswordGraph(appNavigation)
    itemDetailGraph(appNavigation)
}
