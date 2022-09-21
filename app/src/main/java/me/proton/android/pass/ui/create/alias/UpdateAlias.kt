package me.proton.android.pass.ui.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
internal fun UpdateAlias(
    shareId: ShareId,
    itemId: ItemId,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit
) {
    val viewModel: UpdateAliasViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        viewModel.onStart(shareId, itemId)
    }
    val viewState by viewModel.aliasUiState.collectAsState()

    AliasContent(
        uiState = viewState,
        topBarTitle = R.string.title_edit_alias,
        canEdit = false,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateAlias() },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxChange = { viewModel.onMailboxChange(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) }
    )
}
