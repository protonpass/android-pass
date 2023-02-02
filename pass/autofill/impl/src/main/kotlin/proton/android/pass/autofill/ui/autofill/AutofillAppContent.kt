package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.navigation.api.rememberAppNavigator

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun AutofillAppContent(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    uiState: AutofillAppUiState,
    onFinished: () -> Unit,
    onAutofillItemClicked: (AutofillItem) -> Unit,
    onItemCreated: (ItemUiModel) -> Unit
) {
    val startDestination = if (uiState.isFingerprintRequired) {
        AutofillNavItem.Auth.route
    } else {
        AutofillNavItem.SelectItem.route
    }

    val appNavigator = rememberAppNavigator()
    AnimatedNavHost(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        navController = appNavigator.navController,
        startDestination = startDestination
    ) {
        appGraph(
            appNavigator = appNavigator,
            autofillAppState = autofillAppState,
            onAutofillItemClicked = onAutofillItemClicked,
            onItemCreated = onItemCreated,
            onFinished = onFinished
        )
    }
}

