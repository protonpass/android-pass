package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun NoteDetail(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    viewModel: NoteDetailViewModel = hiltViewModel(),
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMigrateClick: (ShareId, ItemId) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    when (val state = uiState) {
        NoteDetailUiState.NotInitialised -> {}
        NoteDetailUiState.Error -> LaunchedEffect(Unit) { onUpClick() }
        is NoteDetailUiState.Success -> {
            if (state.isItemSentToTrash) {
                LaunchedEffect(Unit) { onUpClick() }
            }
            val scope = rememberCoroutineScope()
            val bottomSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )
            PassModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    when (state.itemUiModel.state) {
                        ItemState.Active.value -> TopBarOptionsBottomSheetContents(
                            onMigrate = {
                                scope.launch { bottomSheetState.hide() }
                                onMigrateClick(state.itemUiModel.shareId, state.itemUiModel.id)
                            },
                            onMoveToTrash = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onDelete(state.itemUiModel.shareId, state.itemUiModel.id)
                            }
                        )
                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            itemUiModel = state.itemUiModel,
                            onRestoreItem = { shareId, itemId -> },
                            onDeleteItem = { shareId, itemId -> },
                        )
                    }
                }
            ) {
                Scaffold(
                    modifier = modifier,
                    topBar = {
                        ItemDetailTopBar(
                            isLoading = state.isLoading,
                            isInTrash = state.itemUiModel.state == ItemState.Trashed.value,
                            color = PassTheme.colors.noteInteractionNormMajor1,
                            onUpClick = onUpClick,
                            onEditClick = {
                                onEditClick(
                                    state.itemUiModel.shareId,
                                    state.itemUiModel.id,
                                    state.itemUiModel.itemType
                                )
                            },
                            onOptionsClick = {
                                scope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                ) { padding ->
                    NoteContent(
                        modifier = Modifier
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        name = state.itemUiModel.name,
                        note = (state.itemUiModel.itemType as ItemType.Note).text,
                        moreInfoUiState = moreInfoUiState
                    )
                }
            }
        }
    }
}
