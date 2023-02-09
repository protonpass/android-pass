package proton.android.pass.autofill.ui.autofill.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.ui.autofill.AutofillNavItem
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.authGraph(
    appNavigator: AppNavigator,
    selectedAutofillItem: AutofillItem?,
    onAuthSuccess: (AutofillItem) -> Unit,
    onAutofillCancel: () -> Unit
) {
    composable(AutofillNavItem.Auth) {
        BackHandler {
            onAutofillCancel()
        }
        AuthScreen(
            onAuthSuccessful = {
                if (selectedAutofillItem != null) {
                    onAuthSuccess(selectedAutofillItem)
                } else {
                    appNavigator.navigate(AutofillNavItem.SelectItem)
                }
            },
            onAuthFailed = onAutofillCancel,
            onAuthDismissed = onAutofillCancel
        )
    }
}
