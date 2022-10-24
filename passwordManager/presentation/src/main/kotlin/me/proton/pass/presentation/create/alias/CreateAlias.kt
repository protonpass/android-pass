package me.proton.pass.presentation.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.AliasCreated

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateAlias(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: () -> Unit,
    onClose: () -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    val viewState by viewModel.aliasUiState.collectAsStateWithLifecycle()
    val closeScreenState by viewModel.closeScreenEventFlow.collectAsStateWithLifecycle()

    if (closeScreenState is CloseScreenEvent.Close) {
        onClose()
    }

    AliasContent(
        modifier = modifier,
        uiState = viewState,
        topBarTitle = R.string.title_create_alias,
        canEdit = true,
        onUpClick = onUpClick,
        onSuccess = { _, _ ->
            viewModel.onEmitSnackbarMessage(AliasCreated)
            onSuccess()
        },
        onSubmit = { shareId -> viewModel.createAlias(shareId) },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxChange = { viewModel.onMailboxChange(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}
