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

package proton.android.pass.features.secure.links.overview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewLinkIdNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class SecureLinksOverviewViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSecureLink: ObserveSecureLink,
    observeItemById: ObserveItemById,
    observeVaultById: GetVaultById,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val secureLinkId: SecureLinkId = savedStateHandleProvider.get()
        .require<String>(SecureLinksOverviewLinkIdNavArgId.key)
        .let(::SecureLinkId)

    private val secureLinkFlow = observeSecureLink(secureLinkId = secureLinkId)

    private val itemUiModelFlow = secureLinkFlow.flatMapLatest { secureLink ->
        observeItemById(shareId = secureLink.shareId, itemId = secureLink.itemId)
    }.map { item ->
        encryptionContextProvider.withEncryptionContext {
            item.toUiModel(this@withEncryptionContext).copy(isPinned = false)
        }
    }

    private val vaultFlow = secureLinkFlow.flatMapLatest { secureLink ->
        observeVaultById(shareId = secureLink.shareId)
    }

    internal val state: StateFlow<SecureLinksOverviewState> = combine(
        secureLinkFlow,
        itemUiModelFlow,
        vaultFlow,
        userPreferencesRepository.getUseFaviconsPreference()
    ) { secureLink, itemUiModel, vault, useFavIconsPreference ->
        SecureLinksOverviewState(
            secureLinkUrl = secureLink.url,
            expirationSeconds = secureLink.expirationInSeconds,
            maxViewsAllowed = secureLink.maxReadCount,
            itemUiModel = itemUiModel,
            canLoadExternalImages = useFavIconsPreference.value(),
            shareIcon = vault.icon
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SecureLinksOverviewState.Initial
    )

    internal fun onLinkCopied() {
        clipboardManager.copyToClipboard(text = state.value.secureLinkUrl, isSecure = false)

        viewModelScope.launch {
            snackbarDispatcher(SecureLinksOverviewSnackbarMessage.LinkCopied)
        }
    }

}
