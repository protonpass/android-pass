package proton.android.pass.featureitemcreate.impl.alias.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featureitemcreate.impl.alias.AliasDraftSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.CloseScreenEvent

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateAliasBottomSheet(
    modifier: Modifier = Modifier,
    itemTitle: String,
    onAliasCreated: (AliasItem) -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateAliasBottomSheetViewModel = hiltViewModel()
) {
    LaunchedEffect(itemTitle) {
        viewModel.setInitialState(itemTitle)
    }

    val state by viewModel.aliasUiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.closeScreenEvent) {
        if (state.closeScreenEvent is CloseScreenEvent.Close) {
            onCancel()
        }
    }

    val isAliasDraftSaved = state.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(state.selectedVault) {
            state.selectedVault?.let {
                onAliasCreated(isAliasDraftSaved.aliasItem)
                viewModel.resetAliasDraftSavedState()
            }
        }
    }

    CreateAliasBottomSheetContent(
        modifier = modifier,
        state = state,
        onCancel = onCancel,
        onConfirm = {
            state.selectedVault?.let {
                viewModel.createAlias(it.vault.shareId)
            }
        },
        onPrefixChanged = { viewModel.onPrefixChange(it) },
        onSuffixChanged = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) }
    )
}
