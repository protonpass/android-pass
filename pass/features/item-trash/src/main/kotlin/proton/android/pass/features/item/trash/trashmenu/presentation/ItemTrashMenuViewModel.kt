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

package proton.android.pass.features.item.trash.trashmenu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class ItemTrashMenuViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    userPreferencesRepository: UserPreferencesRepository,
    getItemById: GetItemById,
    encryptionContextProvider: EncryptionContextProvider,
    private val restoreItem: RestoreItems,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val actionFlow = MutableStateFlow<BottomSheetItemAction>(BottomSheetItemAction.None)

    private val eventFlow = MutableStateFlow<ItemTrashMenuEvent>(ItemTrashMenuEvent.Idle)

    private val canLoadExternalImagesFlow = userPreferencesRepository.getUseFaviconsPreference()
        .map { favIconsPreference ->
            favIconsPreference.value()
        }

    private val itemUiModelOptionFlow = oneShot { getItemById(shareId, itemId) }
        .map { item ->
            encryptionContextProvider.withEncryptionContext {
                item.toUiModel(this@withEncryptionContext).toOption()
            }
        }

    internal val state: StateFlow<ItemTrashMenuState> = combine(
        actionFlow,
        eventFlow,
        canLoadExternalImagesFlow,
        itemUiModelOptionFlow,
        ::ItemTrashMenuState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemTrashMenuState.Initial
    )

    internal fun onConsumeEvent(event: ItemTrashMenuEvent) {
        eventFlow.compareAndSet(event, ItemTrashMenuEvent.Idle)
    }

    internal fun onLeaveItem() {
        eventFlow.update { ItemTrashMenuEvent.OnLeaveItem(shareId) }
    }

    internal fun onDeleteItem() {
        eventFlow.update { ItemTrashMenuEvent.OnDeleteItem(shareId, itemId) }
    }

    internal fun onRestoreItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Restore }

            runCatching { restoreItem(items = mapOf(shareId to listOf(itemId))) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error restoring an item from trash")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemTrashMenuEvent.OnItemRestoreError }
                    snackbarDispatcher(ItemTrashMenuSnackBarMessage.ItemRestoreError)
                }
                .onSuccess {
                    eventFlow.update { ItemTrashMenuEvent.OnItemRestored }
                    snackbarDispatcher(ItemTrashMenuSnackBarMessage.ItemRestored)
                }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    private companion object {

        private const val TAG = "ItemTrashMenuViewModel"

    }

}
