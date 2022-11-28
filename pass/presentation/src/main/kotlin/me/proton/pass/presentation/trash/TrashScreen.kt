package me.proton.pass.presentation.trash

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.pass.presentation.components.model.ItemUiModel

@ExperimentalMaterialApi
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    onDrawerIconClick: () -> Unit,
    viewModel: TrashScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (currentBottomSheet, setBottomSheet) = remember { mutableStateOf(TrashBottomSheetType.ItemActions) }
    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val (selectedItem, setSelectedItem) = remember { mutableStateOf<ItemUiModel?>(null) }
    val scope = rememberCoroutineScope()

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                TrashBottomSheetType.ItemActions -> TrashItemBottomSheetContents(itemUiModel = selectedItem)
                TrashBottomSheetType.AllTrashActions -> TrashAllBottomSheetContents()
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
    }
}
