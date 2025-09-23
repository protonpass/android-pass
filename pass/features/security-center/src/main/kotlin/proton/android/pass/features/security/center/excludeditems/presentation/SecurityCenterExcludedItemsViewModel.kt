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

package proton.android.pass.features.security.center.excludeditems.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.features.security.center.PassMonitorDisplayExcludedItems
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class SecurityCenterExcludedItemsViewModel @Inject constructor(
    observeItems: ObserveItems,
    observeVaultsGroupedByShareId: ObserveVaultsGroupedByShareId,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
    telemetryManager: TelemetryManager
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayExcludedItems)
    }

    private val excludedLoginItemsUiModelFlow: Flow<List<ItemUiModel>> = observeItems(
        selection = ShareSelection.AllShares,
        filter = ItemTypeFilter.Logins,
        itemState = ItemState.Active,
        itemFlags = mapOf(ItemFlag.SkipHealthCheck to true)
    ).map { excludedLoginItems ->
        encryptionContextProvider.withEncryptionContext {
            excludedLoginItems.map { excludedLoginItem ->
                excludedLoginItem.toUiModel(this@withEncryptionContext).copy(isPinned = false)
            }
        }
    }

    internal val state: StateFlow<SecurityCenterExcludedItemsState> = combine(
        excludedLoginItemsUiModelFlow,
        userPreferencesRepository.getUseFaviconsPreference(),
        observeVaultsGroupedByShareId(includeHidden = false)
    ) { excludedLoginItemsUiModels, useFavIconsPreference, groupedVaults ->
        SecurityCenterExcludedItemsState(
            excludedItemUiModels = excludedLoginItemsUiModels,
            canLoadExternalImages = useFavIconsPreference.value(),
            isLoading = false,
            groupedVaults = groupedVaults
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterExcludedItemsState.Initial
    )

}
