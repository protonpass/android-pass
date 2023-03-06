package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.navigation.api.rememberAppNavigator

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialNavigationApi::class
)
@Composable
fun AutofillAppContent(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    isFingerprintRequired: Boolean,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit,
) {
    val startDestination = remember {
        if (isFingerprintRequired) {
            Auth.route
        } else {
            SelectItem.route
        }
    }

    val viewModel = hiltViewModel<AutofillAppViewModel>()
    val appNavigator = rememberAppNavigator()
    AnimatedNavHost(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        navController = appNavigator.navController,
        startDestination = startDestination
    ) {
        appGraph(
            appNavigator = appNavigator,
            autofillAppState = autofillAppState,
            selectedAutofillItem = selectedAutofillItem,
            onAutofillSuccess = onAutofillSuccess,
            onAutofillCancel = onAutofillCancel,
            onAutofillItemReceived = { autofillItem ->
                onAutofillSuccess(viewModel.getMappings(autofillItem, autofillAppState))
            }
        )
    }
}

