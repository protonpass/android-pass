package proton.android.pass.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.navigation.appGraph

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Suppress("LongParameterList")
fun PassNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    coreNavigation: CoreNavigation,
    dismissBottomSheet: (() -> Unit) -> Unit,
    finishActivity: () -> Unit,
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
            coreNavigation = coreNavigation,
            finishActivity = finishActivity,
            dismissBottomSheet = dismissBottomSheet,
        )
    }
}
