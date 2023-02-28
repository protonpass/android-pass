package proton.android.pass.featurecreateitem.impl.alias.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurecreateitem.impl.alias.CreateAliasViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateAliasBottomSheet(
    modifier: Modifier = Modifier,
    itemTitle: String,
    onCancel: () -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    LaunchedEffect(itemTitle) {
        viewModel.onTitleChange(itemTitle)
    }

    val state by viewModel.aliasUiState.collectAsStateWithLifecycle()
    CreateAliasBottomSheetContent(
        modifier = modifier,
        state = state,
        onCancel = onCancel,
        onConfirm = {},
        onPrefixChanged = { viewModel.onAliasChange(it) },
        onSuffixChanged = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) }
    )
}
