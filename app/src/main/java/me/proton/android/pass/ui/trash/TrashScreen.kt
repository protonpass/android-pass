package me.proton.android.pass.ui.trash

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.pass.presentation.components.common.PassSnackbarHost
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState

@ExperimentalMaterialApi
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    onDrawerIconClick: () -> Unit
) {
    val viewModel: TrashScreenViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { PassSnackbarHostState() }
    val snackbarMessages = TrashSnackbarMessage.values()
        .associateWith { stringResource(id = it.id) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage
            .collectLatest { message ->
                scope.launch {
                    snackbarMessages[message]?.let {
                        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                    }
                }
            }
    }

    TrashContent(
        modifier = modifier,
        uiState = uiState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = snackbarHostState) },
        onRestoreItem = { viewModel.restoreItem(it) },
        onDeleteItem = { viewModel.deleteItem(it) },
        onDrawerIconClick = onDrawerIconClick,
        onClearTrash = { viewModel.clearTrash() },
        onRefresh = { viewModel.onRefresh() }
    )
}
