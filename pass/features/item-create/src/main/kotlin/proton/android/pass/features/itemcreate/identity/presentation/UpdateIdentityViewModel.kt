/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.identity.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.areItemContentsEqual
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.canDisplaySharedItemWarningDialogFlow
import proton.android.pass.features.itemcreate.common.canDisplayVaultSharedWarningDialogFlow
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.InitError
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemUpdateError
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemUpdated
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class UpdateIdentityViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val identityActionsProvider: IdentityActionsProvider,
    private val telemetryManager: TelemetryManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    observeItemById: ObserveItemById,
    private val settingsRepository: InternalSettingsRepository
) : ViewModel(), IdentityActionsProvider by identityActionsProvider {

    private val navShareId: ShareId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ShareId.key)
            .let(::ShareId)
    private val navItemId: ItemId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ItemId.key)
            .let(::ItemId)

    var originalPersonalCustomFields: List<UICustomFieldContent> = emptyList()
    var originalAddressCustomFields: List<UICustomFieldContent> = emptyList()
    var originalContactCustomFields: List<UICustomFieldContent> = emptyList()
    var originalWorkCustomFields: List<UICustomFieldContent> = emptyList()
    var originalSections: List<UIExtraSection> = emptyList()

    private val canDisplayVaultSharedWarningDialogFlow =
        canDisplayVaultSharedWarningDialogFlow(
            settingsRepository = settingsRepository,
            observeShare = observeShare,
            shareId = navShareId
        )

    private val canDisplaySharedItemWarningDialogFlow =
        canDisplaySharedItemWarningDialogFlow(
            settingsRepository = settingsRepository,
            observeItemById = observeItemById,
            shareId = navShareId,
            itemId = navItemId
        )

    init {
        viewModelScope.launch {
            identityActionsProvider.observeActions(this)
            launch { getItem() }
        }
    }

    val state: StateFlow<IdentityUiState> = combine(
        identityActionsProvider.observeSharedState(),
        identityActionsProvider.observeReceivedItem().map { it is Some },
        canDisplayVaultSharedWarningDialogFlow,
        canDisplaySharedItemWarningDialogFlow
    ) { sharedState: IdentitySharedUiState, hasReceivedItem,
        canDisplayVaultSharedWarningDialog, canDisplaySharedItemWarningDialog ->
        IdentityUiState.UpdateIdentity(
            navShareId,
            sharedState,
            hasReceivedItem,
            canDisplayVaultSharedWarningDialog,
            canDisplaySharedItemWarningDialog
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IdentityUiState.NotInitialised
        )

    private suspend fun getItem() {
        identityActionsProvider.updateLoadingState(IsLoadingState.Loading)
        runCatching { getItemById(shareId = navShareId, itemId = navItemId) }
            .onSuccess { item: Item ->
                identityActionsProvider.onItemReceivedState(item)
                getFormState().let {
                    originalPersonalCustomFields = it.uiPersonalDetails.customFields
                    originalAddressCustomFields = it.uiAddressDetails.customFields
                    originalContactCustomFields = it.uiContactDetails.customFields
                    originalWorkCustomFields = it.uiWorkDetails.customFields
                    originalSections = it.uiExtraSections
                }
            }
            .onFailure {
                PassLogger.i(TAG, it, "Get by id error")
                snackbarDispatcher(InitError)
            }
        identityActionsProvider.updateLoadingState(IsLoadingState.NotLoading)
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    fun onSubmit(shareId: ShareId) = viewModelScope.launch {
        if (!identityActionsProvider.isFormStateValid()) return@launch
        identityActionsProvider.updateLoadingState(IsLoadingState.Loading)
        runCatching {
            val item = identityActionsProvider.getReceivedItem()
            val contents = identityActionsProvider.getFormState().toItemContents()
            val userId = accountManager.getPrimaryUserId().first()
                ?: throw IllegalStateException("User id is null")
            val hasContentsChanged = encryptionContextProvider.withEncryptionContextSuspendable {
                !areItemContentsEqual(
                    a = item.toItemContents { decrypt(it) },
                    b = contents,
                    decrypt = { decrypt(it) }
                )
            }
            val hasPendingAttachments =
                pendingAttachmentLinkRepository.getAllToLink().isNotEmpty() ||
                    pendingAttachmentLinkRepository.getAllToUnLink().isNotEmpty()
            if (hasContentsChanged || hasPendingAttachments) {
                updateItem(
                    userId = userId,
                    shareId = shareId,
                    item = item,
                    contents = contents
                )
            } else {
                item
            }
        }.onSuccess { item ->
            snackbarDispatcher(ItemUpdated)
            identityActionsProvider.onItemSavedState(item)
            telemetryManager.sendEvent(ItemCreate(EventItemType.Identity))
        }.onFailure {
            PassLogger.w(TAG, "Could not update item")
            PassLogger.w(TAG, it)
            snackbarDispatcher(ItemUpdateError)
        }
        identityActionsProvider.updateLoadingState(IsLoadingState.NotLoading)
    }

    fun onOpenAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch { identityActionsProvider.openAttachment(contextHolder, attachment) }
    }

    fun onRetryUploadDraftAttachment(metadata: FileMetadata) {
        viewModelScope.launch { identityActionsProvider.retryUploadDraftAttachment(metadata) }
    }

    override fun onCleared() {
        identityActionsProvider.clearDraftData()
        super.onCleared()
    }

    fun dismissFileAttachmentsOnboarding() {
        viewModelScope.launch { identityActionsProvider.dismissFileAttachmentsOnboardingBanner() }
    }

    fun onPasteTotp() {
        viewModelScope.launch { identityActionsProvider.pasteTotp() }
    }

    companion object {
        private const val TAG = "UpdateIdentityViewModel"
    }
}

