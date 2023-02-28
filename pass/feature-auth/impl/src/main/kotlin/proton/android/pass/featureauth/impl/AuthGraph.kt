package proton.android.pass.featureauth.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Auth : NavItem(baseRoute = "auth")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.authGraph(
    onNavigateBack: () -> Unit,
    onAuthSuccessful: () -> Unit,
    onAuthDismissed: () -> Unit,
    onAuthFailed: () -> Unit
) {
    composable(Auth) {
        BackHandler { onNavigateBack() }
        AuthScreen(
            onAuthSuccessful = { onAuthSuccessful() },
            onAuthFailed = { onAuthFailed() },
            onAuthDismissed = { onAuthDismissed() }
        )
    }
}
