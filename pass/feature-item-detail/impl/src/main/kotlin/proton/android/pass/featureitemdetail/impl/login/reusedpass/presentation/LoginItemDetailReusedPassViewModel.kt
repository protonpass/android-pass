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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import javax.inject.Inject

@HiltViewModel
class LoginItemDetailReusedPassViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItemById: ObserveItemById,
    observeVaultsGroupedByShareId: ObserveVaultsGroupedByShareId,
    userPreferencesRepository: UserPreferencesRepository,
    duplicatedPasswordChecker: DuplicatedPasswordChecker,
    encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val duplicatedLoginItemsFlow = observeItemById(shareId, itemId)
        .map { loginItem ->
            duplicatedPasswordChecker(loginItem).let { duplicatedPasswordReport ->
                encryptionContextProvider.withEncryptionContext {
                    duplicatedPasswordReport.duplications.map { duplicatedPassLoginItem ->
                        duplicatedPassLoginItem.toUiModel(this@withEncryptionContext)
                    }
                }
            }
        }

    internal val state: StateFlow<LoginItemDetailReusedPassState> = combine(
        observeItemById(shareId, itemId),
        duplicatedLoginItemsFlow,
        userPreferencesRepository.getUseFaviconsPreference(),
        observeVaultsGroupedByShareId(),
    ) { item, duplicatedLoginItems, useFavIconsPreference, groupedVaults ->
        LoginItemDetailReusedPassState(
            password = "",
            duplicatedPasswordLoginItems = duplicatedLoginItems.toImmutableList(),
            canLoadExternalImages = useFavIconsPreference.value(),
            groupedVaults = groupedVaults
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LoginItemDetailReusedPassState.Initial
    )

}
