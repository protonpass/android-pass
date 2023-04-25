package proton.android.pass.featurefeatureflags.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object FeatureFlagRoute : NavItem(baseRoute = "feature-flags")

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.featureFlagsGraph() {
    composable(FeatureFlagRoute) {
        FeatureFlagsScreen()
    }
}
