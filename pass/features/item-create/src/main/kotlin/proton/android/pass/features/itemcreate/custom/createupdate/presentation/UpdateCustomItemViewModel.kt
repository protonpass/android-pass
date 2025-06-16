/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.areItemContentsEqual
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.CustomItemFormProcessor
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class UpdateCustomItemViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val telemetryManager: TelemetryManager,
    private val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val attachmentsHandler: AttachmentsHandler,
    private val renameAttachments: RenameAttachments,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    customFieldHandler: CustomFieldHandler,
    customItemFormProcessor: CustomItemFormProcessor,
    canPerformPaidAction: CanPerformPaidAction,
    linkAttachmentsToItem: LinkAttachmentsToItem,
    userPreferencesRepository: UserPreferencesRepository,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    customFieldDraftRepository: CustomFieldDraftRepository,
    clipboardManager: ClipboardManager,
    appDispatchers: AppDispatchers,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCustomItemViewModel(
    canPerformPaidAction = canPerformPaidAction,
    linkAttachmentsToItem = linkAttachmentsToItem,
    attachmentsHandler = attachmentsHandler,
    customFieldHandler = customFieldHandler,
    snackbarDispatcher = snackbarDispatcher,
    userPreferencesRepository = userPreferencesRepository,
    featureFlagsRepository = featureFlagsRepository,
    encryptionContextProvider = encryptionContextProvider,
    customFieldDraftRepository = customFieldDraftRepository,
    clipboardManager = clipboardManager,
    customItemFormProcessor = customItemFormProcessor,
    appDispatchers = appDispatchers,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val navShareId: ShareId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ShareId.key)
            .let(::ShareId)
    private val navItemId: ItemId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ItemId.key)
            .let(::ItemId)

    private var receivedItem: Option<Item> = None
    private var originalCustomFields: List<UICustomFieldContent> = emptyList()
    private var originalSections: List<UIExtraSection> = emptyList()

    init {
        processIntent(UpdateSpecificIntent.LoadInitialData)
    }

    val state = observeSharedState()
        .map { CustomItemState.UpdateCustomItemState(navShareId.some(), it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomItemState.NotInitialised
        )

    fun processIntent(intent: BaseItemFormIntent) {
        when (intent) {
            is BaseCustomItemCommonIntent -> processCommonIntent(intent)
            is UpdateSpecificIntent -> processSpecificIntent(intent)
            else -> throw IllegalArgumentException("Unknown intent: $intent")
        }
    }

    private fun processSpecificIntent(intent: UpdateSpecificIntent) {
        when (intent) {
            is UpdateSpecificIntent.SubmitUpdate -> onSubmitUpdate()
            is UpdateSpecificIntent.LoadInitialData -> onLoadInitialData()
            is UpdateSpecificIntent.OnOpenAttachment ->
                onOpenAttachment(intent.contextHolder, intent.attachment)
        }
    }

    private fun onSubmitUpdate() {
        viewModelScope.launch {
            if (!isFormStateValid(originalCustomFields, originalSections)) return@launch
            updateLoadingState(IsLoadingState.Loading)
            runCatching {
                val userId = accountManager.getPrimaryUserId().first()
                    ?: throw IllegalStateException("User id is null")
                val item = receivedItem.value()
                    ?: throw IllegalStateException("Item is null")
                val updatedContents: ItemContents = itemFormState.toItemContents()
                val hasContentsChanged = encryptionContextProvider.withEncryptionContextSuspendable {
                    !areItemContentsEqual(
                        a = item.toItemContents { decrypt(it) },
                        b = updatedContents,
                        decrypt = { decrypt(it) }
                    )
                }
                val hasPendingAttachments =
                    pendingAttachmentLinkRepository.getAllToLink().isNotEmpty() ||
                        pendingAttachmentLinkRepository.getAllToUnLink().isNotEmpty()
                if (hasContentsChanged || hasPendingAttachments) {
                    updateItem(
                        userId = userId,
                        shareId = navShareId,
                        item = item,
                        contents = updatedContents
                    )
                } else {
                    item
                }
            }.onSuccess { item ->
                snackbarDispatcher(CustomItemSnackbarMessage.ItemUpdated)
                linkAttachments(item.shareId, item.id, item.revision)
                onRenameAttachments(item.shareId, item.id)
                onItemSavedState(item)
                telemetryManager.sendEvent(ItemCreate(EventItemType.Custom))
            }.onFailure {
                PassLogger.w(TAG, "Could not update item")
                PassLogger.w(TAG, it)
                snackbarDispatcher(CustomItemSnackbarMessage.ItemUpdateError)
            }
            updateLoadingState(IsLoadingState.NotLoading)
        }
    }

    private fun onLoadInitialData() {
        viewModelScope.launch {
            updateLoadingState(IsLoadingState.Loading)
            runCatching { getItemById(navShareId, navItemId) }
                .onSuccess { onItemReceived(it) }
                .onFailure {
                    PassLogger.i(TAG, it, "Get by id error")
                    snackbarDispatcher(CustomItemSnackbarMessage.InitError)
                }
            updateLoadingState(IsLoadingState.NotLoading)
        }
    }

    private suspend fun onItemReceived(item: Item) {
        runCatching {
            if (item.hasAttachments) {
                attachmentsHandler.getAttachmentsForItem(item.shareId, item.id)
            }
        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Get attachments error")
            snackbarDispatcher(CustomItemSnackbarMessage.AttachmentsInitError)
        }
        receivedItem = item.some()

        encryptionContextProvider.withEncryptionContextSuspendable {
            val formState = when (item.itemType) {
                is ItemType.Custom ->
                    ItemFormState(item.toItemContents<ItemContents.Custom> { decrypt(it) })
                is ItemType.SSHKey ->
                    ItemFormState(item.toItemContents<ItemContents.SSHKey> { decrypt(it) })
                is ItemType.WifiNetwork ->
                    ItemFormState(item.toItemContents<ItemContents.WifiNetwork> { decrypt(it) })
                else -> throw IllegalStateException("Unsupported item type")
            }

            originalCustomFields = formState.customFieldList
            originalSections = formState.sectionList

            val sectionsForEdit = formState.sectionList.map { section ->
                section.copy(
                    customFields = customFieldHandler.sanitiseForEditingCustomFields(section.customFields)
                )
            }
            itemFormState = formState.copy(
                customFieldList = customFieldHandler.sanitiseForEditingCustomFields(formState.customFieldList),
                sectionList = sectionsForEdit
            )
        }
    }

    private fun onOpenAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(
                contextHolder = contextHolder,
                attachment = attachment
            )
        }
    }

    private suspend fun onRenameAttachments(shareId: ShareId, itemId: ItemId) {
        runCatching {
            renameAttachments(shareId, itemId)
        }.onFailure {
            PassLogger.w(TAG, "Error renaming attachments")
            PassLogger.w(TAG, it)
            snackbarDispatcher(CustomItemSnackbarMessage.ItemRenameAttachmentsError)
        }
    }

    companion object {
        private const val TAG = "UpdateCustomItemViewModel"
    }
}

sealed interface UpdateSpecificIntent : BaseItemFormIntent {
    data object SubmitUpdate : UpdateSpecificIntent
    data object LoadInitialData : UpdateSpecificIntent

    data class OnOpenAttachment(
        val contextHolder: ClassHolder<Context>,
        val attachment: Attachment
    ) : UpdateSpecificIntent
}

