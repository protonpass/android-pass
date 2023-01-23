package proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurehome.impl.HomeItemTypeSelection
import proton.android.pass.featurehome.impl.HomeScreenNavigation
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.auth.authGraph
import proton.android.pass.ui.create.alias.createAliasGraph
import proton.android.pass.ui.create.alias.updateAliasGraph
import proton.android.pass.ui.create.login.createLoginGraph
import proton.android.pass.ui.create.login.updateLoginGraph
import proton.android.pass.ui.create.note.createNoteGraph
import proton.android.pass.ui.create.note.updateNoteGraph
import proton.android.pass.ui.create.password.createPasswordGraph
import proton.android.pass.ui.detail.itemDetailGraph
import proton.android.pass.ui.help.helpGraph
import proton.android.pass.ui.home.homeGraph
import proton.android.pass.ui.onboarding.onBoardingGraph
import proton.android.pass.ui.settings.settingsGraph
import proton.android.pass.ui.trash.trashGraph
import proton.android.pass.ui.vault.vaultGraph
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit,
    finishActivity: () -> Unit
) {
    homeGraph(
        navigationDrawer = navigationDrawer,
        homeScreenNavigation = createHomeScreenNavigation(appNavigator),
        onDrawerIconClick = onDrawerIconClick,
        homeItemTypeSelection = homeItemTypeSelection,
        homeVaultSelection = homeVaultSelection
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
    vaultGraph(appNavigator)
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
        toOnBoarding = { appNavigator.navigate(AppNavItem.OnBoarding) },
        toCreatePassword = { appNavigator.navigate(AppNavItem.CreatePassword) }
    )
