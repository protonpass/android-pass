package me.proton.pass.autofill.ui.autofill

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
import me.proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import me.proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillMappings

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    state: AutofillAppState,
    onAutofillResponse: (AutofillMappings?) -> Unit,
    onFinished: () -> Unit
) {
    val viewModel: AutofillAppViewModel = hiltViewModel()
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

    ProtonTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
        ) { padding ->
            AutofillAppContent(
                modifier = Modifier.padding(padding),
                appState = state,
                uiState = uiState,
                onFinished = onFinished,
                onAutofillItemClicked = { viewModel.onAutofillItemClicked(state, it) },
                onItemCreated = { viewModel.onItemCreated(state, it) }
            )
        }
    }
}

