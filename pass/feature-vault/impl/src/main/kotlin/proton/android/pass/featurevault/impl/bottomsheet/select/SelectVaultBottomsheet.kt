package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurevault.impl.VaultNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectVaultBottomsheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: SelectVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SelectVaultUiState.Success -> SelectVaultBottomsheetContent(
            modifier = modifier,
            state = state,
            onVaultClick = { onNavigate(VaultNavigation.VaultSelected(it)) },
            onUpgrade = { onNavigate(VaultNavigation.Upgrade) }
        )

        SelectVaultUiState.Error -> {
            LaunchedEffect(Unit) {
                onNavigate(VaultNavigation.Close)
            }
        }

        SelectVaultUiState.Uninitialised,
        SelectVaultUiState.Loading -> {
            // no-op
        }
    }
}
