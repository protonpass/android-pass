package proton.android.pass.featuremigrate.impl.selectvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.featuremigrate.impl.MigrateNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MigrateSelectVaultBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (MigrateNavigation) -> Unit,
    viewModel: MigrateSelectVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    when (val state = uiState) {
        MigrateSelectVaultUiState.Error -> onNavigate(MigrateNavigation.Close)
        MigrateSelectVaultUiState.Loading,
        MigrateSelectVaultUiState.Uninitialised -> {
            // no-op
        }

        is MigrateSelectVaultUiState.Success -> {
            LaunchedEffect(state.event) {
                val event = state.event
                if (event is Some) {
                    when (val value = event.value) {
                        SelectVaultEvent.Close -> onNavigate(MigrateNavigation.Close)
                        is SelectVaultEvent.VaultSelectedForMigrateItem -> {
                            onNavigate(
                                MigrateNavigation.VaultSelectedForMigrateItem(
                                    sourceShareId = value.sourceShareId,
                                    destShareId = value.destinationShareId,
                                    itemId = value.itemId
                                )
                            )
                        }

                        is SelectVaultEvent.VaultSelectedForMigrateAll -> {
                            onNavigate(
                                MigrateNavigation.VaultSelectedForMigrateAll(
                                    sourceShareId = value.sourceShareId,
                                    destShareId = value.destinationShareId
                                )
                            )
                        }
                    }
                    viewModel.clearEvent()
                }
            }

            MigrateSelectVaultContents(
                modifier = modifier.bottomSheet(),
                vaults = state.vaultList,
                onVaultSelected = { viewModel.onVaultSelected(it) }
            )
        }
    }
}
