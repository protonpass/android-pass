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

package proton.android.pass.featureitemdetail.impl.creditcard

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
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareType
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.common.onEditClick
import proton.android.pass.featureitemdetail.impl.common.onShareClick
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        CreditCardDetailUiState.NotInitialised, CreditCardDetailUiState.Pending -> {}
        CreditCardDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is CreditCardDetailUiState.Success -> {
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
            if (state.isItemSentToTrash || state.isPermanentlyDeleted || state.isRestoredFromTrash) {
                LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
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
                            canMigrate = state.itemActions.canMoveToOtherVault.value(),
                            canMoveToTrash = state.itemActions.canMoveToTrash,
                            isPinned = state.itemContent.model.isPinned,
                            onMigrate = {
                                scope.launch {
                                    bottomSheetState.hide()
                                    viewModel.onMigrate()
                                }
                            },
                            onMoveToTrash = {
                                scope.launch { bottomSheetState.hide() }
                                viewModel.onMoveToTrash(
                                    itemUiModel.shareId,
                                    itemUiModel.id
                                )
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
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
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
                                            shareId = state.share.id
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
                                    when (it.attachmentContentEvent) {
                                        is AttachmentContentEvent.OnAttachmentOpen -> {}
                                        is AttachmentContentEvent.OnAttachmentOptions,
                                        AttachmentContentEvent.OnAddAttachment,
                                        AttachmentContentEvent.OnDeleteAllAttachments,
                                        is AttachmentContentEvent.OnDraftAttachmentOpen,
                                        is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                            throw IllegalStateException("Action not allowed: $it")
                                    }
                            }
                        },
                        isHistoryFeatureEnabled = state.itemFeatures.isHistoryEnabled,
                        isFileAttachmentsEnabled = state.itemFeatures.isFileAttachmentsEnabled,
                        isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                        attachmentsState = state.attachmentsState,
                        hasMoreThanOneVaultShare = state.hasMoreThanOneVault
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
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
