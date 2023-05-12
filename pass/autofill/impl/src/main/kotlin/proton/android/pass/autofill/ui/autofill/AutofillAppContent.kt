package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.launch
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.ui.autofill.navigation.SelectItem
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

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
    onNavigate: (AutofillNavigation) -> Unit
) {
    val startDestination = remember {
        if (isFingerprintRequired) {
            Auth.route
        } else {
            SelectItem.route
        }
    }

    val viewModel = hiltViewModel<AutofillAppViewModel>()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val appNavigator = rememberAppNavigator(
        bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState),
    )
    val coroutineScope = rememberCoroutineScope()
    PassModalBottomSheetLayout(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding(),
        bottomSheetNavigator = appNavigator.bottomSheetNavigator
    ) {
        AnimatedNavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            autofillActivityGraph(
                appNavigator = appNavigator,
                autofillAppState = autofillAppState,
                selectedAutofillItem = selectedAutofillItem,
                onNavigate = onNavigate,
                onAutofillItemReceived = { autofillItem ->
                    val source = if (selectedAutofillItem == null) {
                        // We didn't have an item selected, so the user must have opened the app
                        AutofillTriggerSource.App
                    } else {
                        // We had an item selected
                        AutofillTriggerSource.Source
                    }
                    viewModel.onAutofillItemSelected(source)
                    val mappings = viewModel.getMappings(autofillItem, autofillAppState)
                    onNavigate(AutofillNavigation.Selected(mappings))
                },
                dismissBottomSheet = { callback ->
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        callback()
                    }
                },
            )
        }
    }
}
