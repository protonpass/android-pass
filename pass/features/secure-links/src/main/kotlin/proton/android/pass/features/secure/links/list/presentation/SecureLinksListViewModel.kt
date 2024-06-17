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

package proton.android.pass.features.secure.links.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.securelink.ObserveUnexpiredSecureLinks
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class SecureLinksListViewModel @Inject constructor(
    observeUnexpiredSecureLinks: ObserveUnexpiredSecureLinks,
    getItemById: GetItemById,
    encryptionContextProvider: EncryptionContextProvider,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val secureLinksModelsFlow = observeUnexpiredSecureLinks().map { secureLinks ->
        secureLinks.map { secureLink ->
            getItemById(
                shareId = secureLink.shareId,
                itemId = secureLink.itemId
            ).let { item ->
                encryptionContextProvider.withEncryptionContext {
                    SecureLinkModel(
                        itemTitle = decrypt(item.title),
                        itemType = item.itemType,
                        secureLink = secureLink
                    )
                }
            }
        }
    }.catch { error ->
        PassLogger.w(TAG, "There was an error while observing secure links")
        PassLogger.w(TAG, error)
    }

    internal val state: StateFlow<SecureLinksListState> = combine(
        secureLinksModelsFlow,
        userPreferencesRepository.getUseFaviconsPreference()
    ) { secureLinkModels, useFaviconsPreference ->
        SecureLinksListState(
            secureLinksModels = secureLinkModels,
            canLoadExternalImages = useFaviconsPreference.value(),
            isLoadingState = IsLoadingState.NotLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecureLinksListState.Initial
    )

    private companion object {

        private const val TAG = "SecureLinksListViewModel"

    }

}
