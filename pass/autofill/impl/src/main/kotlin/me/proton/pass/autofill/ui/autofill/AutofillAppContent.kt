package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.navigation.api.rememberAppNavigator
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillItem

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun AutofillAppContent(
    modifier: Modifier = Modifier,
    appState: AutofillAppState,
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
            state = appState,
            onAutofillItemClicked = onAutofillItemClicked,
            onItemCreated = onItemCreated,
            onFinished = onFinished
        )
    }
}

