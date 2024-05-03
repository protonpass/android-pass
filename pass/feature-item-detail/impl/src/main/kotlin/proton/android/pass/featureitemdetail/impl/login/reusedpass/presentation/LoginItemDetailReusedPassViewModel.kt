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

package proton.android.pass.featureitemdetail.impl.login.reusedpass.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordChecker
import javax.inject.Inject

@HiltViewModel
class LoginItemDetailReusedPassViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItemById: ObserveItemById,
    observeMonitoredItems: ObserveMonitoredItems,
    observeVaultsGroupedByShareId: ObserveVaultsGroupedByShareId,
    userPreferencesRepository: UserPreferencesRepository,
    repeatedPasswordChecker: RepeatedPasswordChecker,
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    internal val state: StateFlow<LoginItemDetailReusedPassState> = combine(
        observeItemById(shareId, itemId),
        observeMonitoredItems(),
        userPreferencesRepository.getUseFaviconsPreference(),
        observeVaultsGroupedByShareId(),
    ) { item, monitoredItems, useFavIconsPreference, groupedVaults ->

        repeatedPasswordChecker(monitoredItems).also {
            println("JIBIRI: ${it.repeatedPasswordsGroups}")
        }

        LoginItemDetailReusedPassState(
            password = "",
            reusedPasswordItems = persistentListOf(),
            repeatedPasswordsGroups = repeatedPasswordChecker(monitoredItems).repeatedPasswordsGroups,
            canLoadExternalImages = useFavIconsPreference.value(),
            groupedVaults = groupedVaults
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LoginItemDetailReusedPassState.Initial
    )

}
