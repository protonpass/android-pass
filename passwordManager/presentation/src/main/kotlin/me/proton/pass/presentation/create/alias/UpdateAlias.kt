package me.proton.pass.presentation.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.AliasUpdated

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val viewState by viewModel.aliasUiState.collectAsStateWithLifecycle()

    AliasContent(
        modifier = modifier,
        uiState = viewState,
        topBarTitle = R.string.title_edit_alias,
        canEdit = false,
        onUpClick = onUpClick,
        onSuccess = { shareId, itemId ->
            viewModel.onEmitSnackbarMessage(AliasUpdated)
            onSuccess(shareId, itemId)
        },
        onSubmit = { viewModel.updateAlias() },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxChange = { viewModel.onMailboxChange(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}
