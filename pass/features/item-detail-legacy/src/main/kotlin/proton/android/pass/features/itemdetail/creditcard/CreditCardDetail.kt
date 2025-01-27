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

package proton.android.pass.features.itemdetail.creditcard

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
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareType
import proton.android.pass.features.itemdetail.ItemDetailNavigation
import proton.android.pass.features.itemdetail.ItemDetailTopBar
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.TopBarOptionsBottomSheetContents
import proton.android.pass.features.itemdetail.common.onEditClick
import proton.android.pass.features.itemdetail.common.onShareClick
import proton.android.pass.features.trash.ConfirmDeleteItemDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents

@Suppress("ComplexMethod")
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun CreditCardDetail(
    modifier: Modifier = Modifier,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: CreditCardDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        CreditCardDetailUiState.NotInitialised, CreditCardDetailUiState.Pending -> {}
        CreditCardDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.CloseScreen) }
        is CreditCardDetailUiState.Success -> {
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

            val itemUiModel = state.itemContent.model

            PassModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    when (itemUiModel.state) {
                        ItemState.Active.value -> TopBarOptionsBottomSheetContents(
                            canMigrate = state.canMigrate,
                            canMoveToTrash = state.canMoveToTrash,
                            canLeave = state.canLeaveItem,
                            isPinned = state.itemContent.model.isPinned,
                            onMigrate = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    viewModel.onMigrate()
                                }
                            },
                            onMoveToTrash = {
                                scope.launch { bottomSheetState.hide() }

                                if (itemUiModel.isShared) {
                                    ItemDetailNavigation.TrashSharedWarning(
                                        shareId = itemUiModel.shareId,
                                        itemId = itemUiModel.id
                                    ).also(onNavigate)
                                } else {
                                    viewModel.onMoveToTrash(
                                        itemUiModel.shareId,
                                        itemUiModel.id
                                    )
                                }
                            },
                            onPinned = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.pinItem(
                                    shareId = itemUiModel.shareId,
                                    itemId = itemUiModel.id
                                )

                            },
                            onUnpinned = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.unpinItem(
                                    shareId = itemUiModel.shareId,
                                    itemId = itemUiModel.id
                                )
                            },
                            onLeave = {
                                scope.launch { bottomSheetState.hide() }

                                ItemDetailNavigation.LeaveItemShare(
                                    shareId = itemUiModel.shareId
                                ).also(onNavigate)
                            }
                        )

                        ItemState.Trashed.value -> TrashItemBottomSheetContents(
                            itemUiModel = itemUiModel,
                            onRestoreItem = { item ->
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onItemRestore(item.shareId, item.id)
                            },
                            onDeleteItem = {
                                scope.launch { bottomSheetState.hide() }
                                shouldShowDeleteItemDialog = true
                            },
                            icon = { CreditCardIcon() }
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
                            actionColor = PassTheme.colors.cardInteractionNormMajor1,
                            iconColor = PassTheme.colors.cardInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                            itemCategory = state.itemContent.model.category,
                            shareSharedCount = state.shareSharedCount,
                            isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                            onUpClick = { onNavigate(ItemDetailNavigation.CloseScreen) },
                            onEditClick = {
                                onEditClick(state.itemActions, onNavigate, state.itemContent.model)
                            },
                            onOptionsClick = {
                                scope.launch { bottomSheetState.show() }
                            },
                            onShareClick = {
                                onShareClick(state.itemActions, onNavigate, state.itemContent.model)
                            }
                        )
                    }
                ) { padding ->
                    CreditCardDetailContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        contents = state.itemContent,
                        share = state.share,
                        isDowngradedMode = state.isDowngradedMode,
                        isPinned = itemUiModel.isPinned,
                        onEvent = {
                            when (it) {
                                CreditCardDetailEvent.OnCardHolderClick -> {
                                    viewModel.copyCardHolderName()
                                }

                                CreditCardDetailEvent.OnCvvClick -> {
                                    viewModel.copyCvv()
                                }

                                CreditCardDetailEvent.OnNumberClick -> {
                                    viewModel.copyNumber()
                                }

                                CreditCardDetailEvent.OnToggleCvvClick -> {
                                    viewModel.toggleCvv()
                                }

                                CreditCardDetailEvent.OnToggleNumberClick -> {
                                    viewModel.toggleNumber()
                                }

                                CreditCardDetailEvent.OnTogglePinClick -> {
                                    viewModel.togglePin()
                                }

                                CreditCardDetailEvent.OnUpgradeClick -> {
                                    onNavigate(ItemDetailNavigation.Upgrade())
                                }

                                CreditCardDetailEvent.OnShareClick -> {
                                    when (state.share.shareType) {
                                        ShareType.Vault -> ItemDetailNavigation.ManageVault(
                                            shareId = state.share.id
                                        )

                                        ShareType.Item -> ItemDetailNavigation.ManageItem(
                                            shareId = state.share.id,
                                            itemId = itemUiModel.id
                                        )
                                    }.also(onNavigate)
                                }

                                CreditCardDetailEvent.OnViewItemHistoryClicked -> onNavigate(
                                    ItemDetailNavigation.OnViewItemHistory(
                                        shareId = state.itemContent.model.shareId,
                                        itemId = state.itemContent.model.id
                                    )
                                )

                                is CreditCardDetailEvent.OnAttachmentEvent ->
                                    when (val event = it.attachmentContentEvent) {
                                        is AttachmentContentEvent.OnAttachmentOpen ->
                                            viewModel.onAttachmentOpen(
                                                contextHolder = context.toClassHolder(),
                                                attachment = event.attachment
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
                        },
                        canViewItemHistory = state.canViewItemHistory,
                        isFileAttachmentsEnabled = state.itemFeatures.isFileAttachmentsEnabled,
                        isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                        attachmentsState = state.attachmentsState,
                        hasMoreThanOneVaultShare = state.hasMoreThanOneVault
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    isSharedItem = state.itemContent.model.isShared,
                    onConfirm = {
                        shouldShowDeleteItemDialog = false
                        viewModel.onPermanentlyDelete(itemUiModel)
                    },
                    onDismiss = { shouldShowDeleteItemDialog = false }
                )
            }
        }
    }
}
