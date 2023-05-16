package proton.android.pass.autofill.ui.autosave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import proton.android.pass.autofill.ui.SnackBarLaunchedEffect
import proton.android.pass.autofill.ui.SnackBarViewModel
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AutoSaveApp(
    modifier: Modifier = Modifier,
    arguments: AutoSaveArguments,
    onAutoSaveSuccess: () -> Unit,
    onAutoSaveCancel: () -> Unit,
    snackBarViewModel: SnackBarViewModel = hiltViewModel(),
    viewModel: AutoSaveAppViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    val snackbarState by snackBarViewModel.state.collectAsStateWithLifecycle()
    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { snackBarViewModel.onSnackbarMessageDelivered() }
    )

    val themePreference by viewModel.state.collectAsStateWithLifecycle()
    val isDark = isDark(themePreference)

    SystemUIEffect(isDark = isDark)

    PassTheme(isDark = isDark) {
        ProvideWindowInsets {
            Scaffold(
                modifier = modifier,
                snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
            ) { padding ->
                AutosaveAppContent(
                    modifier = Modifier
                        .background(PassTheme.colors.backgroundStrong)
                        .systemBarsPadding()
                        .imePadding()
                        .padding(padding),
                    arguments = arguments,
                    onAutoSaveSuccess = {
                        viewModel.onItemAutoSaved()
                        onAutoSaveSuccess()
                    },
                    onAutoSaveCancel = onAutoSaveCancel
                )
            }
        }
    }
}
