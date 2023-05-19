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
import proton.android.pass.featureitemcreate.impl.login.ShareUiState

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

    LaunchedEffect(state.baseAliasUiState.closeScreenEvent) {
        if (state.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            onNavigate(CreateAliasNavigation.CloseBottomsheet)
        }
    }

    val isAliasDraftSaved = state.baseAliasUiState.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(state.shareUiState) {
            if (state.shareUiState is ShareUiState.Success) {
                val event = CreateAliasNavigation.CreatedFromBottomsheet(
                    alias = isAliasDraftSaved.aliasItem.aliasToBeCreated ?: "",
                )
                onNavigate(event)
                viewModel.resetAliasDraftSavedState()
            }
        }
    }

    CreateAliasBottomSheetContent(
        modifier = modifier,
        state = state.baseAliasUiState,
        onCancel = { onNavigate(CreateAliasNavigation.Close) },
        onConfirm = {
            val shareUiState = state.shareUiState
            if (shareUiState is ShareUiState.Success) {
                viewModel.createAlias(shareUiState.currentVault.vault.shareId)
            }
        },
        onPrefixChanged = { viewModel.onPrefixChange(it) },
        onSuffixChanged = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
        onNavigate = onNavigate
    )
}
