package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.autofill.ui.autofill.AutofillNavItem

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
