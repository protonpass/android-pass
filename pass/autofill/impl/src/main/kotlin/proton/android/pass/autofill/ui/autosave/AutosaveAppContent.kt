package proton.android.pass.autofill.ui.autosave

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureauth.impl.AUTH_SCREEN_ROUTE
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun AutosaveAppContent(
    modifier: Modifier = Modifier,
    arguments: AutoSaveArguments,
    onNavigate: (AutosaveNavigation) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val appNavigator = rememberAppNavigator(
        bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState),
    )
    val coroutineScope = rememberCoroutineScope()
    PassModalBottomSheetLayout(
        modifier = Modifier.systemBarsPadding().imePadding(),
        bottomSheetNavigator = appNavigator.bottomSheetNavigator
    ) {
        AnimatedNavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = AUTH_SCREEN_ROUTE,
        ) {
            autosaveActivityGraph(
                appNavigator = appNavigator,
                arguments = arguments,
                onNavigate = onNavigate,
                dismissBottomSheet = { callback ->
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        callback()
                    }
                }
            )
        }
    }
}
