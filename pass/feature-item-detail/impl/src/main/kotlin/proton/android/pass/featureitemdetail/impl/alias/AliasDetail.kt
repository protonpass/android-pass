package proton.android.pass.featureitemdetail.impl.alias

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
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemState

@OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun AliasDetail(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: AliasDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        AliasDetailUiState.NotInitialised -> {}
        AliasDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is AliasDetailUiState.Success -> {
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
                            canMigrate = state.canMigrate,
                            onMigrate = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    onNavigate(
                                        ItemDetailNavigation.OnMigrate(
                                            shareId = state.itemUiModel.shareId,
                                            itemId = state.itemUiModel.id,
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
                            icon = { AliasIcon() }
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
                            actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                            iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
                            onEditClick = { onNavigate(ItemDetailNavigation.OnEdit(state.itemUiModel)) },
                            onOptionsClick = {
                                scope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                ) { padding ->
                    AliasDetailContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        itemUiModel = state.itemUiModel,
                        vault = state.vault,
                        mailboxes = state.mailboxes,
                        isLoading = state.isLoadingMailboxes,
                        onCopyAlias = { viewModel.onCopyAlias(it) },
                        moreInfoUiState = moreInfoUiState,
                        onCreateLoginFromAlias = { alias ->
                            onNavigate(ItemDetailNavigation.OnCreateLoginFromAlias(alias = alias))
                        }
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    onConfirm = {
                        shouldShowDeleteItemDialog = false
                        viewModel.onPermanentlyDelete(state.itemUiModel)
                    },
                    onDismiss = { shouldShowDeleteItemDialog = false }
                )
            }
        }
    }
}
