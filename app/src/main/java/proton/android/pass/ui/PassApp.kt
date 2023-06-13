package proton.android.pass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import proton.android.pass.commonui.api.LifecycleEffect
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    onNavigate: (AppNavigation) -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()
    val isDark = isDark(appUiState.theme)

    SystemUIEffect(isDark = isDark)

    LifecycleEffect(
        onStop = {
            appViewModel.onStop()
        },
        onResume = {
            appViewModel.onResume()
        }
    )

    PassTheme(isDark = isDark) {
        ProvideWindowInsets {
            PassAppContent(
                modifier = modifier
                    .background(PassTheme.colors.backgroundStrong)
                    .systemBarsPadding()
                    .imePadding(),
                appUiState = appUiState,
                onNavigate = onNavigate,
                onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() },
                onAuthPerformed = { }
            )
        }
    }
}
