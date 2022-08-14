package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import me.proton.android.pass.R
import me.proton.android.pass.ui.create.login.CreateLoginView
import me.proton.android.pass.ui.create.login.UpdateLoginView
import me.proton.android.pass.ui.create.note.CreateNoteView
import me.proton.android.pass.ui.create.note.UpdateNoteView
import me.proton.android.pass.ui.create.password.CreatePasswordView
import me.proton.android.pass.ui.detail.ItemDetailScreen
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.launcher.LauncherScreen
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AppNavGraph(
    keyStoreCrypto: KeyStoreCrypto,
    launcherViewModel: LauncherViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
) {
    val navController = rememberAnimatedNavController(keyStoreCrypto)
    AnimatedNavHost(
        navController = navController,
        startDestination = NavItem.Launcher.route,
    ) {
        mainScreenNavigation(navController, launcherViewModel, onDrawerStateChanged)
        crudNavigation(navController)
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
private fun NavGraphBuilder.mainScreenNavigation(
    navController: NavHostController,
    launcherViewModel: LauncherViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
) {
    composable(NavItem.Launcher) {
        LauncherScreen(
            onDrawerStateChanged = onDrawerStateChanged,
            viewModel = launcherViewModel,
            homeScreenNavigation = object : HomeScreenNavigation {
                override val toCreateLogin = { shareId: ShareId ->
                    navController.navigate(NavItem.CreateLogin.createNavRoute(shareId))
                }
                override val toEditLogin = { shareId: ShareId, itemId: ItemId ->
                    navController.navigate(NavItem.EditLogin.createNavRoute(shareId, itemId))
                }
                override val toItemDetail = { shareId: ShareId, itemId: ItemId ->
                    navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId))
                }
                override val toCreateNote = { shareId: ShareId ->
                    navController.navigate(NavItem.CreateNote.createNavRoute(shareId))
                }
                override val toEditNote = { shareId: ShareId, itemId: ItemId ->
                    navController.navigate(NavItem.EditNote.createNavRoute(shareId, itemId))
                }
                override val toCreateAlias = { shareId: ShareId ->
                }
                override val toEditAlias = { shareId: ShareId, itemId: ItemId ->
                    /* TODO */
                }
                override val toCreatePassword = { shareId: ShareId ->
                    navController.navigate(NavItem.CreatePassword.createNavRoute(shareId))
                }
            }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
private fun NavGraphBuilder.crudNavigation(
    navController: NavHostController,
) {
    val onUpClick: () -> Unit = { navController.popBackStack() }
    composable(NavItem.CreateLogin) {
        val shareId = ShareId(it.findArg(NavArg.ShareId))
        CreateLoginView(
            onUpClick = onUpClick,
            shareId = shareId,
            onSuccess = { itemId ->
                navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId)) {
                    popUpTo(NavItem.Launcher.route)
                }
            }
        )
    }
    composable(NavItem.EditLogin) {
        val shareId = ShareId(it.findArg(NavArg.ShareId))
        val itemId = ItemId(it.findArg(NavArg.ItemId))
        UpdateLoginView(
            onUpClick = onUpClick,
            shareId = shareId,
            itemId = itemId,
            onSuccess = {
                navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId)) {
                    popUpTo(NavItem.Launcher.route)
                }
            }
        )
    }
    composable(NavItem.CreateNote) {
        val shareId = ShareId(it.findArg(NavArg.ShareId))
        CreateNoteView(
            onUpClick = onUpClick,
            shareId = shareId,
            onSuccess = { itemId ->
                navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId)) {
                    popUpTo(NavItem.Launcher.route)
                }
            }
        )
    }
    composable(NavItem.EditNote) {
        val shareId = ShareId(it.findArg(NavArg.ShareId))
        val itemId = ItemId(it.findArg(NavArg.ItemId))
        UpdateNoteView(
            onUpClick = onUpClick,
            shareId = shareId,
            itemId = itemId,
            onSuccess = {
                navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId)) {
                    popUpTo(NavItem.Launcher.route)
                }
            }
        )
    }
    composable(NavItem.ViewItem) {
        ItemDetailScreen(
            onUpClick = onUpClick,
            shareId = it.findArg(NavArg.ShareId),
            itemId = it.findArg(NavArg.ItemId)
        )
    }
    composable(NavItem.CreatePassword) {
        CreatePasswordView(
            topBarTitle = R.string.title_create_password,
            onUpClick = onUpClick,
            onConfirm = {}
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.composable(
    navItem: NavItem,
    animate: Boolean = true,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = navItem.route,
        arguments = navItem.args,
        enterTransition = { if (animate) slideInHorizontally(initialOffsetX = { 1000 }) else null },
        exitTransition = { if (animate) slideOutHorizontally(targetOffsetX = { -1000 }) else null },
        popEnterTransition = { if (animate) slideInHorizontally(initialOffsetX = { -1000 }) else null },
        popExitTransition = { if (animate) slideOutHorizontally(targetOffsetX = { 1000 }) else null },
    ) {
        content(it)
    }
}

private inline fun <reified T> NavBackStackEntry.findArg(arg: NavArg): T {
    val value = arguments?.get(arg.key)
    requireNotNull(value)
    return value as T
}
