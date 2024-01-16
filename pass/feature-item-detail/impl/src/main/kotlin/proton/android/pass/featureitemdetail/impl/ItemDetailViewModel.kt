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

package proton.android.pass.featureitemdetail.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
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
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clock: Clock,
    private val telemetryManager: TelemetryManager,
    getItemById: GetItemById,
    savedStateHandle: SavedStateHandleProvider,
    userPreferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))

    val uiState: StateFlow<ItemDetailScreenUiState> = combine(
        oneShot { getItemById(shareId, itemId) }.asLoadingResult(),
        userPreferenceRepository.getUseFaviconsPreference()
    ) { result, favicons ->
        when (result) {
            is LoadingResult.Error -> {
                PassLogger.e(TAG, result.exception, "Get by id error")
                snackbarDispatcher(DetailSnackbarMessages.InitError)
                ItemDetailScreenUiState.Initial
            }

            LoadingResult.Loading -> ItemDetailScreenUiState.Initial
            is LoadingResult.Success -> ItemDetailScreenUiState(
                itemTypeUiState = when (result.data.itemType) {
                    is ItemType.Login -> ItemTypeUiState.Login
                    is ItemType.Note -> ItemTypeUiState.Note
                    is ItemType.Alias -> ItemTypeUiState.Alias
                    ItemType.Password -> ItemTypeUiState.Password
                    is ItemType.CreditCard -> ItemTypeUiState.CreditCard
                    ItemType.Unknown -> ItemTypeUiState.Unknown
                },
                moreInfoUiState = getMoreInfoUiState(result.data),
                canLoadExternalImages = favicons.value()
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
            ItemTypeUiState.Unknown -> null
        }
        eventItemType?.let {
            telemetryManager.sendEvent(ItemRead(eventItemType))
        }
    }

    private fun getMoreInfoUiState(item: Item): MoreInfoUiState = MoreInfoUiState(
        now = clock.now(),
        createdTime = item.createTime,
        lastAutofilled = item.lastAutofillTime,
        lastModified = item.modificationTime,
        numRevisions = item.revision
    )

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
