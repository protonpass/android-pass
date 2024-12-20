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

package proton.android.pass.featureitemdetail.impl.login

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareType
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.onEditClick
import proton.android.pass.featureitemdetail.impl.common.onShareClick
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.TopBarOptions
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.WebsiteOptions
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.LoginTopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.WebsiteOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldEvent
import proton.android.pass.features.trash.ConfirmDeleteItemDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Suppress("ComplexMethod")
fun LoginDetail(
    modifier: Modifier = Modifier,
    canLoadExternalImages: Boolean,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: LoginDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        LoginDetailUiState.NotInitialised, LoginDetailUiState.Pending -> {}
        LoginDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is LoginDetailUiState.Success -> {
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

            var currentBottomSheet by remember { mutableStateOf(WebsiteOptions) }
            var selectedWebsite by remember { mutableStateOf("") }

            PassModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    when (currentBottomSheet) {
                        WebsiteOptions -> WebsiteOptionsBottomSheetContents(
                            website = selectedWebsite,
                            onCopyToClipboard = { website ->
                                viewModel.copyWebsiteToClipboard(website)
                                scope.launch { bottomSheetState.hide() }
                            },
                            onOpenWebsite = { website ->
                                openWebsite(context, website)
                                scope.launch { bottomSheetState.hide() }
                            }
                        )

                        TopBarOptions -> when (state.itemUiModel.state) {
                            ItemState.Active.value -> LoginTopBarOptionsBottomSheetContents(
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
                                    viewModel.onMoveToTrash(
                                        state.itemUiModel.shareId,
                                        state.itemUiModel.id
                                    )
                                    scope.launch { bottomSheetState.hide() }
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
                                onExcludeFromMonitoring = {
                                    scope.launch { bottomSheetState.hide() }
                                    viewModel.onExcludeItemFromMonitoring()
                                },
                                onIncludeInMonitoring = {
                                    scope.launch { bottomSheetState.hide() }
                                    viewModel.onIncludeItemInMonitoring()
                                },
                                onLeave = {
                                    scope.launch { bottomSheetState.hide() }
                                },
                                isExcludedFromMonitor = state.monitorState.isExcludedFromMonitor
                            )

                            ItemState.Trashed.value -> TrashItemBottomSheetContents(
                                itemUiModel = state.itemUiModel,
                                onRestoreItem = { item ->
                                    scope.launch { bottomSheetState.hide() }
                                    viewModel.onItemRestore(item.shareId, item.id)
                                },
                                onDeleteItem = {
                                    scope.launch { bottomSheetState.hide() }
                                    shouldShowDeleteItemDialog = true
                                },
                                icon = {
                                    val contents = state.itemUiModel.contents as ItemContents.Login
                                    val sortedPackages =
                                        contents.packageInfoSet.sortedBy { it.packageName.value }
                                    val packageName =
                                        sortedPackages.firstOrNull()?.packageName?.value
                                    val website = contents.urls.firstOrNull()
                                    LoginIcon(
                                        text = contents.title,
                                        canLoadExternalImages = canLoadExternalImages,
                                        website = website,
                                        packageName = packageName
                                    )
                                }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    modifier = modifier,
                    topBar = {
                        ItemDetailTopBar(
                            isLoading = state.isLoading,
                            actions = state.itemActions,
                            actionColor = PassTheme.colors.loginInteractionNormMajor1,
                            iconColor = PassTheme.colors.loginInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                            itemCategory = state.itemUiModel.category,
                            shareSharedCount = state.shareSharedCount,
                            isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
                            onEditClick = {
                                onEditClick(state.itemActions, onNavigate, state.itemUiModel)
                            },
                            onOptionsClick = {
                                currentBottomSheet = TopBarOptions
                                scope.launch { bottomSheetState.show() }
                            },
                            onShareClick = {
                                onShareClick(state.itemActions, onNavigate, state.itemUiModel)
                            }
                        )
                    }
                ) { padding ->
                    LoginContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        itemUiModel = state.itemUiModel,
                        passwordScore = state.passwordScore,
                        share = state.share,
                        showViewAlias = state.linkedAlias.isNotEmpty(),
                        totpUiState = state.totpUiState,
                        canLoadExternalImages = canLoadExternalImages,
                        customFields = state.customFields,
                        passkeys = state.passkeys,
                        monitorState = state.monitorState,
                        attachmentsState = state.attachmentsState,
                        onEvent = {
                            when (it) {
                                LoginDetailEvent.OnCopyPasswordClick -> {
                                    viewModel.copyPasswordToClipboard()
                                }

                                is LoginDetailEvent.OnCopyTotpClick -> {
                                    viewModel.copyTotpCodeToClipboard(it.totpCode)
                                }

                                is LoginDetailEvent.OnCustomFieldEvent -> {
                                    when (val event = it.event) {
                                        is CustomFieldEvent.CopyValue -> {
                                            viewModel.copyCustomFieldValue(event.index)
                                        }

                                        is CustomFieldEvent.ToggleFieldVisibility -> {
                                            viewModel.toggleCustomFieldVisibility(event.index)
                                        }

                                        is CustomFieldEvent.CopyValueContent -> {
                                            viewModel.copyCustomFieldContent(event.content)
                                        }

                                        CustomFieldEvent.Upgrade -> {
                                            onNavigate(ItemDetailNavigation.Upgrade())
                                        }
                                    }
                                }

                                LoginDetailEvent.OnGoToAliasClick -> {
                                    state.linkedAlias.map { aliasItem ->
                                        onNavigate(
                                            ItemDetailNavigation.OnViewItem(
                                                shareId = aliasItem.shareId,
                                                itemId = aliasItem.itemId
                                            )
                                        )
                                    }
                                }

                                LoginDetailEvent.OnTogglePasswordClick -> {
                                    viewModel.togglePassword()
                                }

                                LoginDetailEvent.OnUpgradeClick -> {
                                    onNavigate(ItemDetailNavigation.Upgrade())
                                }

                                is LoginDetailEvent.OnEmailClick -> {
                                    viewModel.copyEmailToClipboard(it.email)
                                }

                                LoginDetailEvent.OnUsernameClick -> {
                                    viewModel.copyUsernameToClipboard()
                                }

                                is LoginDetailEvent.OnWebsiteClicked -> {
                                    openWebsite(context, it.website)
                                }

                                is LoginDetailEvent.OnWebsiteLongClicked -> {
                                    selectedWebsite = it.website
                                    currentBottomSheet = WebsiteOptions
                                    scope.launch { bottomSheetState.show() }
                                }

                                LoginDetailEvent.OnShareClick -> {
                                    when (state.share.shareType) {
                                        ShareType.Vault -> ItemDetailNavigation.ManageVault(
                                            shareId = state.share.id
                                        )

                                        ShareType.Item -> ItemDetailNavigation.ManageItem(
                                            shareId = state.share.id,
                                            itemId = state.itemUiModel.id
                                        )
                                    }.also(onNavigate)
                                }

                                LoginDetailEvent.OnViewItemHistoryClicked -> onNavigate(
                                    ItemDetailNavigation.OnViewItemHistory(
                                        shareId = state.itemUiModel.shareId,
                                        itemId = state.itemUiModel.id
                                    )
                                )

                                is LoginDetailEvent.OnSelectPasskey -> {
                                    onNavigate(
                                        ItemDetailNavigation.ViewPasskeyDetails(
                                            shareId = state.itemUiModel.shareId,
                                            itemId = state.itemUiModel.id,
                                            passkeyId = PasskeyId(it.passkey.id)
                                        )
                                    )
                                }

                                LoginDetailEvent.OnShowReusedPasswords -> {
                                    ItemDetailNavigation.ViewReusedPasswords(
                                        shareId = state.itemUiModel.shareId,
                                        itemId = state.itemUiModel.id
                                    ).also(onNavigate)
                                }

                                is LoginDetailEvent.OnAttachmentEvent ->
                                    when (val event = it.attachmentContentEvent) {
                                        is AttachmentContentEvent.OnAttachmentOpen ->
                                            viewModel.onAttachmentOpen(
                                                contextHolder = context.toClassHolder(),
                                                attachment = event.attachment
                                            )

                                        is AttachmentContentEvent.OnAttachmentOptions,
                                        AttachmentContentEvent.OnAddAttachment,
                                        AttachmentContentEvent.OnDeleteAllAttachments,
                                        is AttachmentContentEvent.OnDraftAttachmentOpen,
                                        is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                            throw IllegalStateException("Action not allowed: $it")
                                    }
                            }
                        },
                        canViewItemHistory = state.canViewItemHistory,
                        isFileAttachmentsEnabled = state.itemFeatures.isFileAttachmentsEnabled,
                        isItemSharingEnabled = state.itemFeatures.isItemSharingEnabled,
                        hasMoreThanOneVaultShare = state.hasMoreThanOneVault
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
