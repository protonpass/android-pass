package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurevault.impl.VaultNavigation

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun VaultOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: VaultOptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is VaultOptionsUiState.Success -> VaultOptionsBottomSheetContents(
            modifier = modifier,
            state = state,
            onEdit = { onNavigate(VaultNavigation.VaultEdit(state.shareId)) },
            onMigrate = { onNavigate(VaultNavigation.VaultMigrate(state.shareId)) },
            onRemove = { onNavigate(VaultNavigation.VaultRemove(state.shareId)) }
        )

        VaultOptionsUiState.Loading,
        VaultOptionsUiState.Uninitialised -> {
            // no-op
        }

        VaultOptionsUiState.Error -> {
            LaunchedEffect(Unit) {
                onNavigate(VaultNavigation.Close)
            }
        }
    }
}
