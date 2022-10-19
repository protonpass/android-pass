package me.proton.android.pass.ui.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.PassSnackbarHost
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState
import me.proton.core.pass.presentation.uievents.IsSentToTrashState

@ExperimentalComposeUiApi
@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMovedToTrash: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSentToTrash) {
        if (uiState.isSentToTrash == IsSentToTrashState.Sent) {
            onMovedToTrash()
        }
    }
    val snackbarHostState = remember { PassSnackbarHostState() }
    val snackbarMessages = DetailSnackbarMessages.values()
        .associateWith { stringResource(id = it.id) }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage
            .collectLatest { message ->
                coroutineScope.launch {
                    snackbarMessages[message]?.let {
                        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                    }
                }
            }
    }
    Scaffold(
        snackbarHost = { PassSnackbarHost(snackbarHostState = snackbarHostState) }
    ) { innerPadding ->
        ItemDetailContent(
            modifier = modifier.padding(innerPadding),
            uiState = uiState,
            onUpClick = onUpClick,
            onEditClick = onEditClick,
            onMoveToTrash = { item: Item ->
                viewModel.sendItemToTrash(item)
            },
            onSnackbarMessage = { message ->
                coroutineScope.launch {
                    snackbarMessages[message]?.let {
                        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                    }
                }
            }
        )
    }
}

