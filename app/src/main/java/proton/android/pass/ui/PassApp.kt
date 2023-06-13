package proton.android.pass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

    val currentConfig = LocalConfiguration.current
    var orientation by rememberSaveable { mutableStateOf(currentConfig.orientation) }

    LifecycleEffect(
        onResume = {
            if (currentConfig.orientation != orientation) {
                appViewModel.onRotate()
                orientation = currentConfig.orientation
            }

        },
        onStop = {
            appViewModel.onStop()
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
