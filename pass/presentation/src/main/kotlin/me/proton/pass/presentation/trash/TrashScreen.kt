package me.proton.pass.presentation.trash

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.pass.presentation.components.model.ItemUiModel

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    onDrawerIconClick: () -> Unit,
    viewModel: TrashScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (currentBottomSheet, setBottomSheet) = remember { mutableStateOf(TrashBottomSheetType.AllTrashActions) }
    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val (selectedItem, setSelectedItem) = remember { mutableStateOf<ItemUiModel?>(null) }
    val (showClearTrashDialog, setShowClearTrashDialog) = rememberSaveable { mutableStateOf(false) }
    val (showRestoreAllDialog, setShowRestoreAllDialog) = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                TrashBottomSheetType.ItemActions -> TrashItemBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onRestoreItem = {
                        viewModel.restoreItem(it)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    },
                    onDeleteItem = {
                        viewModel.deleteItem(it)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    }
                )
                TrashBottomSheetType.AllTrashActions -> TrashAllBottomSheetContents(
                    onEmptyTrash = {
                        setShowClearTrashDialog(true)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    },
                    onRestoreAll = {
                        setShowRestoreAllDialog(true)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    }
                )
            }
        }
    ) {
        TrashContent(
            modifier = modifier,
            uiState = uiState,
            onDeleteItem = { viewModel.deleteItem(it) },
            onTopBarMenuClick = {
                setBottomSheet(TrashBottomSheetType.AllTrashActions)
                scope.launch { bottomSheetState.show() }
            },
            onItemMenuClick = {
                setSelectedItem(it)
                setBottomSheet(TrashBottomSheetType.ItemActions)
                scope.launch { bottomSheetState.show() }
            },
            onDrawerIconClick = onDrawerIconClick,
            onRefresh = { viewModel.onRefresh() }
        )
        ConfirmClearTrashDialog(
            show = showClearTrashDialog,
            onDismiss = { setShowClearTrashDialog(false) },
            onConfirm = { viewModel.clearTrash() }
        )
        ConfirmRestoreAllDialog(
            show = showRestoreAllDialog,
            onDismiss = { setShowRestoreAllDialog(false) },
            onConfirm = { viewModel.restoreItems() }
        )
    }
}
