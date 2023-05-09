package proton.android.pass.featureitemcreate.impl.alias.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featureitemcreate.impl.alias.AliasDraftSavedState
import proton.android.pass.featureitemcreate.impl.alias.CloseScreenEvent
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateAliasBottomSheet(
    modifier: Modifier = Modifier,
    itemTitle: String,
    onNavigate: (CreateAliasNavigation) -> Unit,
    viewModel: CreateAliasBottomSheetViewModel = hiltViewModel()
) {
    LaunchedEffect(itemTitle) {
        viewModel.setInitialState(itemTitle)
    }

    val state by viewModel.createAliasUiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.closeScreenEvent) {
        if (state.closeScreenEvent is CloseScreenEvent.Close) {
            onNavigate(CreateAliasNavigation.Close)
        }
    }

    val isAliasDraftSaved = state.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(state.selectedVault) {
            state.selectedVault?.let {
                val event = CreateAliasNavigation.Created(
                    alias = isAliasDraftSaved.aliasItem.aliasToBeCreated ?: "",
                    dismissBottomsheet = true
                )
                onNavigate(event)
                viewModel.resetAliasDraftSavedState()
            }
        }
    }

    CreateAliasBottomSheetContent(
        modifier = modifier,
        state = state,
        onCancel = { onNavigate(CreateAliasNavigation.Close) },
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
