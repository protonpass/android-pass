package proton.android.pass.autofill.ui.autofill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.SnackBarLaunchedEffect
import proton.android.pass.autofill.ui.SnackBarViewModel
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.preferences.ThemePreference

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    autofillUiState: AutofillUiState.StartAutofillUiState,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit,
    snackBarViewModel: SnackBarViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    val snackbarState by snackBarViewModel.state.collectAsStateWithLifecycle()

    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { snackBarViewModel.onSnackbarMessageDelivered() }
    )

    val isDark = when (ThemePreference.from(autofillUiState.themePreference)) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> isNightMode()
    }
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(systemUiController, isDark) {
        systemUiController.systemBarsDarkContentEnabled = !isDark
    }

    PassTheme(isDark = isDark) {
        ProvideWindowInsets {
            Scaffold(
                modifier = modifier,
                snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
            ) { padding ->
                AutofillAppContent(
                    modifier = Modifier
                        .background(PassTheme.colors.backgroundStrong)
                        .systemBarsPadding()
                        .imePadding()
                        .padding(padding),
                    autofillAppState = autofillUiState.autofillAppState,
                    selectedAutofillItem = autofillUiState.selectedAutofillItem.value(),
                    isFingerprintRequired = autofillUiState.isFingerprintRequiredPreference,
                    onAutofillSuccess = onAutofillSuccess,
                    onAutofillCancel = onAutofillCancel
                )
            }
        }
    }
}

