package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.create.login.createLoginGraph
import me.proton.android.pass.ui.detail.itemDetailGraph
import me.proton.android.pass.ui.help.helpGraph
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.home.homeGraph
import me.proton.android.pass.ui.settings.settingsGraph
import me.proton.android.pass.ui.trash.trashGraph
import me.proton.android.pass.ui.create.alias.createAliasGraph
import me.proton.android.pass.ui.create.alias.updateAliasGraph
import me.proton.android.pass.ui.create.login.updateLoginGraph
import me.proton.android.pass.ui.create.note.createNoteGraph
import me.proton.android.pass.ui.create.note.updateNoteGraph
import me.proton.android.pass.ui.create.password.createPasswordGraph

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
