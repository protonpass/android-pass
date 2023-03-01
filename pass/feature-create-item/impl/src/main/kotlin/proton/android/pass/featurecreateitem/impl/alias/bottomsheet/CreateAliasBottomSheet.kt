package proton.android.pass.featurecreateitem.impl.alias.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurecreateitem.impl.alias.AliasDraftSavedState
import proton.android.pass.featurecreateitem.impl.alias.AliasItem

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
        viewModel.setInitialTitle(itemTitle)
    }

    val state by viewModel.aliasUiState.collectAsStateWithLifecycle()

    val isAliasDraftSaved = state.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(state.selectedShareId) {
            state.selectedShareId?.let {
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
            state.selectedShareId?.let {
                viewModel.createAlias(it.id)
            }
        },
        onPrefixChanged = { viewModel.onPrefixChange(it) },
        onSuffixChanged = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) }
    )
}
