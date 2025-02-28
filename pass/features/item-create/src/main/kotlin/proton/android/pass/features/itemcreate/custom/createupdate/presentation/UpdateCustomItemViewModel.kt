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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class UpdateCustomItemViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val telemetryManager: TelemetryManager,
    private val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    attachmentsHandler: AttachmentsHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCustomItemViewModel(
    attachmentsHandler = attachmentsHandler,
    encryptionContextProvider = encryptionContextProvider,
    customFieldDraftRepository = customFieldDraftRepository,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val navShareId: ShareId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ShareId.key)
            .let(::ShareId)
    private val navItemId: ItemId =
        savedStateHandleProvider.get().require<String>(CommonNavArgId.ItemId.key)
            .let(::ItemId)

    private var receivedItem: Option<Item> = None

    init { processIntent(UpdateSpecificIntent.LoadInitialData) }

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
        }
    }

    private fun onSubmitUpdate() {
        viewModelScope.launch {
            if (!isFormStateValid()) return@launch

            updateLoadingState(IsLoadingState.Loading)
            runCatching {
                val userId = accountManager.getPrimaryUserId().first()
                    ?: throw IllegalStateException("User id is null")
                val item = receivedItem.value()
                    ?: throw IllegalStateException("Item is null")
                val form = itemFormState
                val content = itemFormState.toItemContents()
                updateItem(
                    userId = userId,
                    shareId = navShareId,
                    item = item,
                    contents = itemFormState.toItemContents()
                )
            }.onSuccess { item ->
                onItemSavedState(item)
                telemetryManager.sendEvent(ItemCreate(EventItemType.Custom))
                snackbarDispatcher(CustomItemSnackbarMessage.ItemUpdated)
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
        receivedItem = item.some()
        val itemContents = encryptionContextProvider.withEncryptionContextSuspendable {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.Custom
        }
        itemFormState = ItemFormState(itemContents)
    }

    companion object {
        private const val TAG = "UpdateCustomItemViewModel"
    }
}

sealed interface UpdateSpecificIntent : BaseItemFormIntent {
    data object SubmitUpdate : UpdateSpecificIntent
    data object LoadInitialData : UpdateSpecificIntent
}

