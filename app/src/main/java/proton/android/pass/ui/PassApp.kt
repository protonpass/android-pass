package proton.android.pass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.preferences.ThemePreference

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    coreNavigation: CoreNavigation,
    finishActivity: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {

    val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()

    val isDark = when (appUiState.theme) {
        ThemePreference.Dark -> true
        ThemePreference.Light -> false
        ThemePreference.System -> isNightMode()
    }
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(systemUiController, isDark) {
        systemUiController.systemBarsDarkContentEnabled = !isDark
    }
    PassTheme(isDark = isDark) {
        ProvideWindowInsets {
            PassAppContent(
                modifier = modifier
                    .background(PassTheme.colors.backgroundNorm)
                    .systemBarsPadding()
                    .imePadding(),
                appUiState = appUiState,
                coreNavigation = coreNavigation,
                onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() },
                finishActivity = finishActivity
            )
        }
    }
}
