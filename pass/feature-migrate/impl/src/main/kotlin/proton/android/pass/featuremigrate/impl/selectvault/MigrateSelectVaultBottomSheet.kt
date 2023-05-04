package proton.android.pass.featuremigrate.impl.selectvault

import androidx.activity.compose.BackHandler
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
    navigation: (MigrateNavigation) -> Unit,
    onClose: () -> Unit,
    viewModel: MigrateSelectVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onClose() }

    LaunchedEffect(state.event) {
        val event = state.event
        if (event is Some) {
            when (val value = event.value) {
                SelectVaultEvent.Close -> onClose()
                is SelectVaultEvent.VaultSelectedForMigrateItem -> {
                    navigation(
                        MigrateNavigation.VaultSelectedForMigrateItem(
                            sourceShareId = value.sourceShareId,
                            destShareId = value.destinationShareId,
                            itemId = value.itemId
                        )
                    )
                }
                is SelectVaultEvent.VaultSelectedForMigrateAll -> {
                    navigation(
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
