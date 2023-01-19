package proton.android.pass.featurevault.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun VaultListScreen(
    modifier: Modifier = Modifier,
    viewModel: VaultListViewModel = hiltViewModel(),
    onCreateVault: () -> Unit,
    onEditVault: () -> Unit,
    onUpClick: () -> Unit
) {
    val uiState by viewModel.shareUIState.collectAsStateWithLifecycle()
    VaultListContent(
        modifier = modifier,
        uiState = uiState,
        onVaultCreate = { onCreateVault() },
        onVaultSelect = viewModel::changeSelectedVault,
        onVaultEdit = { onEditVault() },
        onVaultMigrate = { toDelete, toMigrateTo ->
            viewModel.onMigrateVault(toDelete, toMigrateTo)
        },
        onVaultDelete = viewModel::onDeleteVault,
        onUpClick = onUpClick
    )
}
