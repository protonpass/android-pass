/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.domain.ItemState
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.common.onEditClick
import proton.android.pass.featureitemdetail.impl.common.onShareClick
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.ConfirmTrashAliasDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents

@OptIn(
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
        AliasDetailUiState.NotInitialised, AliasDetailUiState.Pending -> {}
        AliasDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is AliasDetailUiState.Success -> {

            LaunchedEffect(state.event) {
                when (state.event) {
                    ItemDetailEvent.Unknown -> {}
                    ItemDetailEvent.MoveToVault -> {
                        onNavigate(ItemDetailNavigation.OnMigrate)
                    }
                }
                viewModel.clearEvent()
            }

            var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
            var shouldShowMoveToTrashItemDialog by rememberSaveable { mutableStateOf(false) }
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
                            canMigrate = state.itemActions.canMoveToOtherVault.value(),
                            canMoveToTrash = state.itemActions.canMoveToTrash,
                            isPinned = state.itemUiModel.isPinned,
                            onMigrate = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    viewModel.onMigrate()
                                }
                            },
                            onMoveToTrash = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    shouldShowMoveToTrashItemDialog = true
                                }
                            },
                            onPinned = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.pinItem(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            },
                            onUnpinned = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.unpinItem(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            }
                        )

                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            itemUiModel = state.itemUiModel,
                            onRestoreItem = { item ->
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onItemRestore(item.shareId, item.id)
                            },
                            onDeleteItem = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    shouldShowDeleteItemDialog = true
                                }
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
                            actions = state.itemActions,
                            actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                            iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
                            onEditClick = {
                                onEditClick(state.itemActions, onNavigate, state.itemUiModel)
                            },
                            onOptionsClick = {
                                scope.launch { bottomSheetState.show() }
                            },
                            onShareClick = {
                                onShareClick(state.itemActions, onNavigate, state.itemUiModel)
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
                            val event = ItemDetailNavigation.OnCreateLoginFromAlias(
                                alias = alias,
                                shareId = state.itemUiModel.shareId
                            )
                            onNavigate(event)
                        },
                        onVaultClick = {
                            state.vault?.shareId?.let {
                                onNavigate(ItemDetailNavigation.ManageVault(it))
                            }
                        },
                        onViewItemHistoryClicked = {
                            onNavigate(
                                ItemDetailNavigation.OnViewItemHistory(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            )
                        },
                        isHistoryFeatureEnabled = state.isHistoryFeatureEnabled
                    )
                }

                ConfirmTrashAliasDialog(
                    show = shouldShowMoveToTrashItemDialog,
                    onConfirm = {
                        shouldShowMoveToTrashItemDialog = false
                        viewModel.onMoveToTrash(
                            state.itemUiModel.shareId,
                            state.itemUiModel.id
                        )
                    },
                    onDismiss = { shouldShowMoveToTrashItemDialog = false }
                )

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
