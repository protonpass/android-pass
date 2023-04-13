package proton.android.pass.autofill.ui.autosave

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
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.preferences.ThemePreference

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AutoSaveApp(
    modifier: Modifier = Modifier,
    info: SaveInformation,
    onAutoSaveSuccess: () -> Unit,
    onAutoSaveCancel: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)
    val viewModel = hiltViewModel<AutoSaveAppViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = when (state) {
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
                AutosaveAppContent(
                    modifier = Modifier
                        .background(PassTheme.colors.backgroundStrong)
                        .systemBarsPadding()
                        .imePadding()
                        .padding(padding),
                    saveInformation = info,
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
