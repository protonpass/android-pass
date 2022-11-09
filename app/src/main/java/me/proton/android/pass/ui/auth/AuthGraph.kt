package me.proton.android.pass.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.auth.AuthScreen

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.authGraph(
    nav: AppNavigator,
    finishActivity: () -> Unit
) {
    composable(NavItem.Auth) {
        BackHandler {
            finishActivity()
        }
        AuthScreen(
            onAuthSuccessful = { nav.onBackClick() },
            onAuthFailed = { nav.onBackClick() }
        )
    }
}
