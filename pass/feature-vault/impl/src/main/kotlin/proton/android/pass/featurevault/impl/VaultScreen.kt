package proton.android.pass.featurevault.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.shareUIState.collectAsStateWithLifecycle()
    VaultContent(
        modifier = modifier,
        uiState = uiState,
        onVaultCreate = { viewModel.onCreateVault() },
        onVaultSelect = viewModel::changeSelectedVault,
        onVaultEdit = {
            // navigate
        },
        onVaultDelete = viewModel::onDeleteVault
    )
}
