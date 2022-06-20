package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import me.proton.android.pass.ui.launcher.LauncherScreen
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.core.crypto.common.keystore.KeyStoreCrypto

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AppNavGraph(
    keyStoreCrypto: KeyStoreCrypto,
    launcherViewModel: LauncherViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
) {
    val navController = rememberAnimatedNavController(keyStoreCrypto)
    AnimatedNavHost(
        navController = navController,
        startDestination = LauncherScreen.route
    ) {
        addLauncher(launcherViewModel, onDrawerStateChanged)
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun NavGraphBuilder.addLauncher(
    launcherViewModel: LauncherViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
) = composable(LauncherScreen.route) {
    LauncherScreen.view(
        onDrawerStateChanged = onDrawerStateChanged,
        viewModel = launcherViewModel,
    )
}
