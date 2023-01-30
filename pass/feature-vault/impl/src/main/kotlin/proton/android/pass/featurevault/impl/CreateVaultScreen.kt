package proton.android.pass.featurevault.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateVaultScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateVaultViewModel = hiltViewModel(),
    onUpClick: () -> Unit
) {
    val uiState by viewModel.createVaultUIState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.vaultSavedState) {
        if (uiState.vaultSavedState == VaultSavedState.Success) {
            onUpClick()
        }
    }
    CreateVaultContent(
        modifier = modifier,
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCreate = viewModel::onCreateVault,
        onUpClick = onUpClick
    )
}
