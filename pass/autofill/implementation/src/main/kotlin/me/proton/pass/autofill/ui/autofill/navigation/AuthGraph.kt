package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.ui.autofill.AutofillNavItem
import me.proton.pass.presentation.auth.AuthScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.authGraph(
    appNavigator: AppNavigator,
    onFinished: () -> Unit
) {
    composable(AutofillNavItem.Auth) {
        AuthScreen(
            onAuthSuccessful = {
                appNavigator.navigate(AutofillNavItem.SelectItem)
            },
            onAuthFailed = onFinished,
            onAuthDismissed = onFinished
        )
    }
}
