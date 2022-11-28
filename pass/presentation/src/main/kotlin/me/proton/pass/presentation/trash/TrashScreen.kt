package me.proton.pass.presentation.trash

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@ExperimentalMaterialApi
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    onDrawerIconClick: () -> Unit,
    viewModel: TrashScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrashContent(
        modifier = modifier,
        uiState = uiState,
        // onRestoreItem = { viewModel.restoreItem(it) },
        onDeleteItem = { viewModel.deleteItem(it) },
        onDrawerIconClick = onDrawerIconClick,
        onClearTrash = { viewModel.clearTrash() },
        onRefresh = { viewModel.onRefresh() }
    )
}
