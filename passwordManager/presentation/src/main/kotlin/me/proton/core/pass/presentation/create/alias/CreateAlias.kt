package me.proton.core.pass.presentation.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateAlias(
    shareId: ShareId,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit
) {
    val viewModel: CreateAliasViewModel = hiltViewModel()
    val viewState by viewModel.aliasUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onStart(shareId)
    }

    AliasContent(
        uiState = viewState,
        topBarTitle = R.string.title_create_alias,
        canEdit = true,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.createAlias(shareId) },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxChange = { viewModel.onMailboxChange(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },

    )
}
