package proton.android.pass.autofill.ui.autofill

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.preferences.ThemePreference

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalPermissionsApi::class)
@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    viewModel: AutofillAppViewModel = hiltViewModel(),
    onAutofillResponse: (AutofillMappings?) -> Unit,
    onFinished: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.itemSelected is AutofillItemSelectedState.Selected) {
        val selected = uiState.itemSelected
        if (selected is AutofillItemSelectedState.Selected) {
            onAutofillResponse(selected.item)
        }
    }

    val isDark = when (uiState.theme) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> isNightMode()
    }

    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    uiState.snackbarMessage?.let { snackbarMessage ->
        val message = stringResource(id = snackbarMessage.id)
        LaunchedEffect(snackbarMessage) {
            passSnackbarHostState.showSnackbar(snackbarMessage.type, message)
            viewModel.onSnackbarMessageDelivered()
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationsPermissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        LaunchedEffect(notificationsPermissionState.status.isGranted) {
            if (!notificationsPermissionState.status.isGranted) {
                notificationsPermissionState.launchPermissionRequest()
            }
        }
    }
    ProtonTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
        ) { padding ->
            AutofillAppContent(
                modifier = Modifier.padding(padding),
                autofillAppState = autofillAppState,
                uiState = uiState,
                onFinished = onFinished,
                onAutofillItemClicked = { viewModel.onAutofillItemClicked(autofillAppState, it) },
                onItemCreated = { viewModel.onItemCreated(autofillAppState, it) }
            )
        }
    }
}

