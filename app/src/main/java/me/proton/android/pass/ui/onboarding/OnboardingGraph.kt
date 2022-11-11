package me.proton.android.pass.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.onboarding.OnBoardingScreen

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.onBoardingGraph(
    nav: AppNavigator,
    finishActivity: () -> Unit
) {
    composable(NavItem.OnBoarding) {
        BackHandler {
            finishActivity()
        }
        OnBoardingScreen { nav.onBackClick() }
    }
}
