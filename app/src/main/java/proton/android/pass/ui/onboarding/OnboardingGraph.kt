package proton.android.pass.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.android.pass.presentation.onboarding.OnBoardingScreen

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
