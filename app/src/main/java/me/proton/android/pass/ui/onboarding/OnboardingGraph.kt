package me.proton.android.pass.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.onboarding.OnBoardingScreen

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.onBoardingGraph(
    nav: AppNavigator,
    finishActivity: () -> Unit
) {
    composable(AppNavItem.OnBoarding) {
        BackHandler {
            finishActivity()
        }
        OnBoardingScreen { nav.onBackClick() }
    }
}
