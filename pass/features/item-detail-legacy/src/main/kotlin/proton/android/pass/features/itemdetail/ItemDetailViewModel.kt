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

package proton.android.pass.features.itemdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.telemetry.api.events.ItemViewed
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val telemetryManager: TelemetryManager,
    getItemById: GetItemById,
    savedStateHandle: SavedStateHandleProvider,
    userPreferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))

    private val itemFlow: Flow<LoadingResult<Item>> = oneShot { getItemById(shareId, itemId) }
        .asLoadingResult()
        .onEach {
            if (it is LoadingResult.Error) {
                eventFlow.update { ItemDetailScreenEvent.Close }
            }
        }

    private val eventFlow: MutableStateFlow<ItemDetailScreenEvent> =
        MutableStateFlow(ItemDetailScreenEvent.Idle)

    val uiState: StateFlow<ItemDetailScreenUiState> = combine(
        itemFlow,
        userPreferenceRepository.getUseFaviconsPreference(),
        eventFlow
    ) { result, favicons, event ->
        when (result) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Get by id error")
                PassLogger.w(TAG, result.exception)
                snackbarDispatcher(DetailSnackbarMessages.InitError)
                ItemDetailScreenUiState.Initial.copy(event = event)
            }

            LoadingResult.Loading -> ItemDetailScreenUiState.Initial
            is LoadingResult.Success -> ItemDetailScreenUiState(
                itemTypeUiState = when (result.data.itemType) {
                    is ItemType.Login -> ItemTypeUiState.Login
                    is ItemType.Note -> ItemTypeUiState.Note
                    is ItemType.Alias -> ItemTypeUiState.Alias
                    ItemType.Password -> ItemTypeUiState.Password
                    is ItemType.CreditCard -> ItemTypeUiState.CreditCard
                    is ItemType.Identity -> ItemTypeUiState.Identity
                    is ItemType.Custom -> ItemTypeUiState.Custom
                    ItemType.Unknown -> ItemTypeUiState.Unknown
                },
                canLoadExternalImages = favicons.value(),
                event = event
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItemDetailScreenUiState.Initial
        )

    fun sendItemReadEvent(itemTypeUiState: ItemTypeUiState) {
        val eventItemType: EventItemType? = when (itemTypeUiState) {
            ItemTypeUiState.Login -> EventItemType.Login
            ItemTypeUiState.Note -> EventItemType.Note
            ItemTypeUiState.Alias -> EventItemType.Alias
            ItemTypeUiState.Password -> EventItemType.Password
            ItemTypeUiState.CreditCard -> EventItemType.CreditCard
            ItemTypeUiState.Identity -> EventItemType.Identity
            ItemTypeUiState.Custom -> EventItemType.Custom
            ItemTypeUiState.Unknown -> null
        }
        eventItemType?.let {
            telemetryManager.sendEvent(ItemRead(eventItemType))
            telemetryManager.sendEvent(ItemViewed(shareId, itemId))
        }
    }

    fun clearEvent() {
        eventFlow.update { ItemDetailScreenEvent.Idle }
    }

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
