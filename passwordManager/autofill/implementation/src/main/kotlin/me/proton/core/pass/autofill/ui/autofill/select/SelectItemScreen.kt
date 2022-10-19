package me.proton.core.pass.autofill.ui.autofill.select

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
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.presentation.components.common.PassSnackbarHost
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState

const val SELECT_ITEM_ROUTE = "autofill/item"

@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    packageName: PackageName,
    onItemSelected: (AutofillItem) -> Unit
) {
    val viewModel: SelectItemViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessages = SelectItemSnackbarMessage.values()
        .associateWith { stringResource(id = it.id) }
    val snackbarHostState = remember { PassSnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.itemClickedEvent is ItemClickedEvent.Clicked) {
        (uiState.itemClickedEvent as? ItemClickedEvent.Clicked)?.let {
            onItemSelected(it.item)
        }
    }

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
    SelectItemScreenContent(
        modifier = modifier,
        state = uiState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = snackbarHostState) },
        onItemClicked = { viewModel.onItemClicked(it, packageName) },
        onRefresh = { viewModel.onRefresh() }
    )
}
