package proton.android.pass.featureonboarding.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object OnBoarding : NavItem(baseRoute = "onboarding")

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.onBoardingGraph(
    onOnBoardingFinished: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(OnBoarding) {
        BackHandler { onNavigateBack() }
        OnBoardingScreen { onOnBoardingFinished() }
    }
}
