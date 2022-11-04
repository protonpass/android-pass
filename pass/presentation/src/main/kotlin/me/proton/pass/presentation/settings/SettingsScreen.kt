package me.proton.pass.presentation.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@ExperimentalMaterialApi
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onDrawerIconClick: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsContent(
        modifier = modifier,
        scaffoldState = scaffoldState,
        onDrawerIconClick = onDrawerIconClick,
        state = state,
        onThemeChange = { viewModel.onThemePreferenceChange(it) },
        onFingerPrintLockChange = { viewModel.onFingerPrintLockChange(it) }
    )
}
