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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.presentation.navigation.CoreNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    coreNavigation: CoreNavigation,
    finishActivity: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        appViewModel.onStart()
    }

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
    ProtonTheme(isDark = isDark) {
        ProvideWindowInsets {
            PassAppContent(
                modifier = modifier
                    .background(ProtonTheme.colors.backgroundNorm)
                    .systemBarsPadding()
                    .imePadding(),
                appUiState = appUiState,
                coreNavigation = coreNavigation,
                onDrawerSectionChanged = { appViewModel.onDrawerSectionChanged(it) },
                onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() },
                finishActivity = finishActivity
            )
        }
    }
}
