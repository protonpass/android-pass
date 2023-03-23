package proton.android.pass.featureitemdetail.impl.migrate.selectvault

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MigrateSelectVaultBottomSheet(
    modifier: Modifier = Modifier,
    onVaultSelected: (ShareId, ItemId, ShareId) -> Unit,
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
                is SelectVaultEvent.SelectedVault -> {
                    onVaultSelected(value.sourceShareId, value.itemId, value.destinationShareId)
                }
            }
            viewModel.clearEvent()
        }
    }

    MigrateSelectVaultContents(
        modifier = modifier.bottomSheetPadding(),
        vaults = state.vaultList,
        onVaultSelected = { viewModel.onVaultSelected(it) }
    )
}
