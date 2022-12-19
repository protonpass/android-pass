package me.proton.pass.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ItemDetailContent(
        modifier = modifier,
        uiState = uiState,
        onUpClick = onUpClick,
        onEditClick = onEditClick
    )
}

