package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType

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
    onNavigate: (ItemDetailNavigation) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    when (val state = uiState) {
        NoteDetailUiState.NotInitialised -> {}
        NoteDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is NoteDetailUiState.Success -> {
            var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
            if (state.isItemSentToTrash || state.isPermanentlyDeleted || state.isRestoredFromTrash) {
                LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
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
                                scope.launch {
                                    bottomSheetState.hide()
                                    onNavigate(
                                        ItemDetailNavigation.OnMigrate(
                                            shareId = state.itemUiModel.shareId,
                                            itemId = state.itemUiModel.id
                                        )
                                    )
                                }
                            },
                            onMoveToTrash = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onMoveToTrash(
                                    state.itemUiModel.shareId,
                                    state.itemUiModel.id
                                )
                            }
                        )
                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            itemUiModel = state.itemUiModel,
                            onRestoreItem = { shareId, itemId ->
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onItemRestore(shareId, itemId)
                            },
                            onDeleteItem = { _, _ ->
                                scope.launch { bottomSheetState.hide() }
                                shouldShowDeleteItemDialog = true
                            },
                            icon = { NoteIcon() }
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
                            actionColor = PassTheme.colors.noteInteractionNormMajor1,
                            iconColor = PassTheme.colors.noteInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.noteInteractionNormMinor2,
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
                            onEditClick = {
                                onNavigate(
                                    ItemDetailNavigation.OnEdit(
                                        shareId = state.itemUiModel.shareId,
                                        itemId = state.itemUiModel.id,
                                        itemType = state.itemUiModel.itemType
                                    )
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
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        name = state.itemUiModel.name,
                        note = (state.itemUiModel.itemType as ItemType.Note).text,
                        vault = state.vault,
                        moreInfoUiState = moreInfoUiState
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    onConfirm = {
                        shouldShowDeleteItemDialog = false
                        viewModel.onPermanentlyDelete(
                            state.itemUiModel.shareId,
                            state.itemUiModel.id,
                            state.itemUiModel.itemType
                        )
                    },
                    onDismiss = { shouldShowDeleteItemDialog = false }
                )
            }
        }
    }
}
