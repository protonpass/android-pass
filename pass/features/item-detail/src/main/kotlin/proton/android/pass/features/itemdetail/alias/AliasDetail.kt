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

package proton.android.pass.features.itemdetail.alias

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
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareType
import proton.android.pass.features.itemdetail.ItemDetailNavigation
import proton.android.pass.features.itemdetail.ItemDetailTopBar
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.TopBarOptionsBottomSheetContents
import proton.android.pass.features.itemdetail.common.onEditClick
import proton.android.pass.features.itemdetail.common.onShareClick
import proton.android.pass.features.trash.ConfirmDeleteDisabledAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteEnabledAliasDialog
import proton.android.pass.features.trash.ConfirmTrashAliasDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun AliasDetail(
    modifier: Modifier = Modifier,
    isItemMovedToTrash: Boolean,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: AliasDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
                viewModel.onConsumeEvent(state.event)
            }

            var shouldShowMoveToTrashItemDialog by rememberSaveable { mutableStateOf(false) }
            var shouldShowRemoveEnabledAliasDialog by rememberSaveable { mutableStateOf(false) }
            var shouldShowRemoveDisabledAliasDialog by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(state.isLoadingMap) {
                if (!state.isAnyLoading) {
                    shouldShowMoveToTrashItemDialog = false
                    shouldShowRemoveEnabledAliasDialog = false
                    shouldShowRemoveDisabledAliasDialog = false
                }
            }

            if (state.requiresBackNavigation || isItemMovedToTrash) {
                LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
            }
            val scope = rememberCoroutineScope()
            val bottomSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )

            val contents = state.itemUiModel.contents as ItemContents.Alias

            PassModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    when (state.itemUiModel.state) {
                        ItemState.Active.value -> TopBarOptionsBottomSheetContents(
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
                                if (state.itemFeatures.slAliasSyncEnabled) {
                                    if (contents.isEnabled && !state.itemFeatures.isAliasTrashDialogChecked) {
                                        ItemDetailNavigation.OnTrashAlias(
                                            shareId = state.itemUiModel.shareId,
                                            itemId = state.itemUiModel.id
                                        ).also(onNavigate)
                                    } else {
                                        viewModel.onMoveToTrash(
                                            state.itemUiModel.shareId,
                                            state.itemUiModel.id
                                        )
                                    }
                                } else {
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
                            },
                            onLeave = {
                                scope.launch { bottomSheetState.hide() }

                                ItemDetailNavigation.LeaveItemShare(
                                    shareId = state.itemUiModel.shareId
                                ).also(onNavigate)
                            }
                        )

                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            itemUiModel = state.itemUiModel,
                            onRestoreItem = { item ->
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onItemRestore(item.shareId, item.id)
                            },
                            onDeleteItem = {
                                scope.launch { bottomSheetState.hide() }
                                if (contents.isEnabled) {
                                    shouldShowRemoveEnabledAliasDialog = true
                                } else {
                                    shouldShowRemoveDisabledAliasDialog = true
                                }
                            },
                            icon = {
                                AliasIcon(
                                    activeAlias = contents.isEnabled
                                )
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    modifier = modifier,
                    topBar = {
                        ItemDetailTopBar(
                            isLoading = state.isAnyLoading,
                            actions = state.itemActions,
                            actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                            iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                            itemCategory = state.itemUiModel.category,
                            shareSharedCount = state.shareSharedCount,
                            isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
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
                        share = state.share,
                        mailboxes = state.mailboxes,
                        isAliasCreatedByUser = state.isAliasCreatedByUser,
                        slNote = state.slNote,
                        displayName = state.displayName,
                        stats = state.stats,
                        contactsCount = state.contactsCount,
                        isLoading = state.isLoadingMailboxes,
                        canViewItemHistory = state.canViewItemHistory,
                        isAliasSyncEnabled = state.itemFeatures.slAliasSyncEnabled,
                        isAliasStateToggling = state.isLoading(LoadingStateKey.AliasStateToggling),
                        isAliasManagementEnabled = state.itemFeatures.isAliasManagementEnabled,
                        isFileAttachmentsEnabled = state.itemFeatures.isFileAttachmentsEnabled,
                        isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                        attachmentsState = state.attachmentsState,
                        hasMoreThanOneVaultShare = state.hasMoreThanOneVault,
                        onCopyAlias = { viewModel.onCopyAlias(it) },
                        onCreateLoginFromAlias = { alias ->
                            val event = ItemDetailNavigation.OnCreateLoginFromAlias(
                                alias = alias,
                                shareId = state.itemUiModel.shareId
                            )
                            onNavigate(event)
                        },
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
                        onViewItemHistoryClicked = {
                            onNavigate(
                                ItemDetailNavigation.OnViewItemHistory(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            )
                        },
                        onToggleAliasState = {
                            viewModel.toggleAliasState(
                                shareId = state.itemUiModel.shareId,
                                itemId = state.itemUiModel.id,
                                state = it
                            )
                        },
                        onContactsClicked = {
                            onNavigate(
                                ItemDetailNavigation.OnContactsClicked(
                                    shareId = state.itemUiModel.shareId,
                                    itemId = state.itemUiModel.id
                                )
                            )
                        },
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
                                is AttachmentContentEvent.OnDraftAttachmentOpen,
                                is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                    throw IllegalStateException("Action not allowed: $it")
                            }
                        }
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

                ConfirmDeleteEnabledAliasDialog(
                    show = shouldShowRemoveEnabledAliasDialog,
                    isDeleteLoading = state.isLoading(LoadingStateKey.PermanentlyDeleting),
                    isDisableLoading = state.isLoading(LoadingStateKey.AliasStateToggling),
                    alias = (state.itemUiModel.contents as ItemContents.Alias).aliasEmail,
                    onDelete = {
                        viewModel.onPermanentlyDelete(state.itemUiModel)
                    },
                    onDisable = {
                        viewModel.toggleAliasState(
                            shareId = state.itemUiModel.shareId,
                            itemId = state.itemUiModel.id,
                            state = false
                        )
                    },
                    onDismiss = {
                        shouldShowRemoveEnabledAliasDialog = false
                    }
                )

                ConfirmDeleteDisabledAliasDialog(
                    show = shouldShowRemoveDisabledAliasDialog,
                    isLoading = state.isLoading(LoadingStateKey.PermanentlyDeleting),
                    alias = (state.itemUiModel.contents as ItemContents.Alias).aliasEmail,
                    onConfirm = {
                        viewModel.onPermanentlyDelete(state.itemUiModel)
                    },
                    onDismiss = {
                        shouldShowRemoveDisabledAliasDialog = false
                    }
                )
            }
        }
    }
}
