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
import me.proton.android.pass.ui.home.homeGraph
import me.proton.android.pass.ui.onboarding.onBoardingGraph
import me.proton.android.pass.ui.settings.settingsGraph
import me.proton.android.pass.ui.trash.trashGraph
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.home.HomeFilterMode
import me.proton.pass.presentation.home.HomeScreenNavigation

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    homeFilterMode: HomeFilterMode,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit,
    finishActivity: () -> Unit
) {
    homeGraph(
        navigationDrawer = navigationDrawer,
        homeScreenNavigation = createHomeScreenNavigation(appNavigator),
        onDrawerIconClick = onDrawerIconClick,
        homeFilterMode = homeFilterMode
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

private fun createHomeScreenNavigation(appNavigator: AppNavigator): HomeScreenNavigation =
    HomeScreenNavigation(
        toCreateLogin = { shareId ->
            appNavigator.navigate(
                AppNavItem.CreateLogin,
                AppNavItem.CreateLogin.createNavRoute(shareId)
            )
        },
        toEditLogin = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                AppNavItem.EditLogin,
                AppNavItem.EditLogin.createNavRoute(shareId, itemId)
            )
        },
        toCreateNote = { shareId ->
            appNavigator.navigate(
                AppNavItem.CreateNote,
                AppNavItem.CreateNote.createNavRoute(shareId)
            )
        },
        toEditNote = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                AppNavItem.EditNote,
                AppNavItem.EditNote.createNavRoute(shareId, itemId)
            )
        },
        toCreateAlias = { shareId ->
            appNavigator.navigate(
                AppNavItem.CreateAlias,
                AppNavItem.CreateAlias.createNavRoute(shareId)
            )
        },
        toEditAlias = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                AppNavItem.EditAlias,
                AppNavItem.EditAlias.createNavRoute(shareId, itemId)
            )
        },
        toItemDetail = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                AppNavItem.ViewItem,
                AppNavItem.ViewItem.createNavRoute(shareId, itemId)
            )
        },
        toAuth = { appNavigator.navigate(AppNavItem.Auth) },
        toOnBoarding = {
            appNavigator.navigate(AppNavItem.OnBoarding)
        },
        toCreatePassword = { shareId ->
            appNavigator.navigate(
                AppNavItem.CreatePassword,
                AppNavItem.CreatePassword.createNavRoute(shareId)
            )
        }
    )
