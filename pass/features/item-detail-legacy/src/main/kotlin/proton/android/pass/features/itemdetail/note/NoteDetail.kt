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

package proton.android.pass.features.itemdetail.note

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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareType
import proton.android.pass.features.itemdetail.ItemDetailNavigation
import proton.android.pass.features.itemdetail.ItemDetailTopBar
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.onEditClick
import proton.android.pass.features.itemdetail.common.onShareClick
import proton.android.pass.features.trash.ConfirmDeleteItemDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun NoteDetail(
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = hiltViewModel(),
    onNavigate: (ItemDetailNavigation) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    when (val state = uiState) {
        NoteDetailUiState.NotInitialised, NoteDetailUiState.Pending -> {}
        NoteDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.CloseScreen) }
        is NoteDetailUiState.Success -> {
            LaunchedEffect(state.event) {
                when (state.event) {
                    ItemDetailEvent.Unknown -> Unit
                    ItemDetailEvent.MoveToVault -> {
                        onNavigate(ItemDetailNavigation.OnMigrate)
                    }

                    ItemDetailEvent.MoveToVaultSharedWarning -> {
                        onNavigate(ItemDetailNavigation.OnMigrateSharedWarning)
                    }
                }

                viewModel.clearEvent()
            }

            var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
            if (state.isItemSentToTrash || state.isPermanentlyDeleted || state.isRestoredFromTrash) {
                LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.CloseScreen) }
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
                        ItemState.Active.value -> NoteTopBarOptionsBottomSheetContents(
                            canMigrate = state.canMigrate,
                            canMoveToTrash = state.canMoveToTrash,
                            canLeave = state.canLeaveItem,
                            isPinned = state.itemUiModel.isPinned,
                            onMigrate = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    viewModel.onMigrate()
                                }
                            },
                            onMoveToTrash = {
                                scope.launch { bottomSheetState.hide() }

                                viewModel.onMoveToTrash(
                                    state.itemUiModel.shareId,
                                    state.itemUiModel.id
                                )
                            },
                            onCopyNote = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    viewModel.onCopyToClipboard(state.itemUiModel)
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
                            },
                            onLeave = {
                                scope.launch { bottomSheetState.hide() }

                                ItemDetailNavigation.LeaveItemShare(
                                    shareId = state.itemUiModel.shareId
                                ).also(onNavigate)
                            }
                        )

                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            canBeDeleted = state.share.canBeDeleted,
                            itemUiModel = state.itemUiModel,
                            onLeaveItem = { item ->
                                scope.launch { bottomSheetState.hide() }

                                ItemDetailNavigation.LeaveItemShare(
                                    shareId = item.shareId
                                ).also(onNavigate)
                            },
                            onRestoreItem = { item ->
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onItemRestore(item.shareId, item.id)
                            },
                            onDeleteItem = {
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
                            actions = state.itemActions,
                            actionColor = PassTheme.colors.noteInteractionNormMajor1,
                            iconColor = PassTheme.colors.noteInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                            itemCategory = state.itemUiModel.category,
                            shareSharedCount = state.shareSharedCount,
                            isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                            onUpClick = { onNavigate(ItemDetailNavigation.CloseScreen) },
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
                    NoteContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        itemUiModel = state.itemUiModel,
                        share = state.share,
                        onShareClick = {
                            when (state.share.shareType) {
                                ShareType.Vault -> ItemDetailNavigation.ManageVault(
                                    shareId = state.share.id
                                )

                                ShareType.Item -> ItemDetailNavigation.ManageItem(
                                    shareId = state.share.id,
                                    itemId = state.itemUiModel.id
                                )
                            }.also(onNavigate)
                        },
                        isPinned = state.itemUiModel.isPinned,
                        onViewItemHistoryClicked = {
                            onNavigate(
                                ItemDetailNavigation.OnViewItemHistory(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            )
                        },
                        attachmentsState = state.attachmentsState,
                        canViewItemHistory = state.canViewItemHistory,
                        isFileAttachmentsEnabled = state.itemFeatures.isFileAttachmentsEnabled,
                        isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                        hasMoreThanOneVaultShare = state.hasMoreThanOneVault,
                        onAttachmentEvent = {
                            when (it) {
                                is AttachmentContentEvent.OnAttachmentOpen ->
                                    viewModel.onAttachmentOpen(
                                        contextHolder = context.toClassHolder(),
                                        attachment = it.attachment
                                    )

                                is AttachmentContentEvent.OnAttachmentOptions,
                                AttachmentContentEvent.OnAddAttachment,
                                AttachmentContentEvent.OnDeleteAllAttachments,
                                AttachmentContentEvent.UpsellAttachments,
                                is AttachmentContentEvent.OnDraftAttachmentOpen,
                                is AttachmentContentEvent.OnDraftAttachmentRetry,
                                is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                    throw IllegalStateException("Action not allowed: $it")

                            }
                        }
                    )
                }

                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    isSharedItem = state.itemUiModel.isShared,
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
