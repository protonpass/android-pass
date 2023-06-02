package proton.android.pass.featuretrial.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

sealed interface TrialNavigation {
    object Close : TrialNavigation
    object Upgrade : TrialNavigation
}

object TrialScreen : NavItem(baseRoute = "trial/screen")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.trialGraph(
    onNavigate: (TrialNavigation) -> Unit
) {
    composable(TrialScreen) {
        TrialScreen(
            onNavigate = onNavigate
        )
    }
}
