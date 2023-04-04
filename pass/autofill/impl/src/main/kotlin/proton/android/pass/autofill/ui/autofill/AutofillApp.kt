package proton.android.pass.autofill.ui.autofill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.preferences.ThemePreference

@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    autofillUiState: AutofillUiState.StartAutofillUiState,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

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

