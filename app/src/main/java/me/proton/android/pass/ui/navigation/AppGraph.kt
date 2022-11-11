package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.auth.authGraph
import me.proton.android.pass.ui.create.alias.createAliasGraph
import me.proton.android.pass.ui.create.alias.updateAliasGraph
import me.proton.android.pass.ui.create.login.createLoginGraph
import me.proton.android.pass.ui.create.login.updateLoginGraph
import me.proton.android.pass.ui.create.note.createNoteGraph
import me.proton.android.pass.ui.create.note.updateNoteGraph
import me.proton.android.pass.ui.create.password.createPasswordGraph
import me.proton.android.pass.ui.detail.itemDetailGraph
import me.proton.android.pass.ui.help.helpGraph
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.home.homeGraph
import me.proton.android.pass.ui.onboarding.onBoardingGraph
import me.proton.android.pass.ui.settings.settingsGraph
import me.proton.android.pass.ui.trash.trashGraph

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit,
    finishActivity: () -> Unit
) {
    homeGraph(
        navigationDrawer,
        HomeScreenNavigation(appNavigator),
        onDrawerIconClick
    )
    trashGraph(navigationDrawer, onDrawerIconClick)
    helpGraph(navigationDrawer, onDrawerIconClick)
    settingsGraph(navigationDrawer, onDrawerIconClick)
    createLoginGraph(appNavigator)
    updateLoginGraph(appNavigator)
    createNoteGraph(appNavigator)
    updateNoteGraph(appNavigator)
    createAliasGraph(appNavigator)
    updateAliasGraph(appNavigator)
    createPasswordGraph(appNavigator)
    itemDetailGraph(appNavigator)
    authGraph(appNavigator, finishActivity)
    onBoardingGraph(appNavigator, finishActivity)
}
