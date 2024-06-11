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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLinkExpiration
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewExpirationNavArgId
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewLinkNavArgId
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewMaxViewsNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecureLinksOverviewViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val expiration = savedStateHandleProvider.get()
        .require<SecureLinkExpiration>(SecureLinksOverviewExpirationNavArgId.key)

    private val maxViewsAllowed: Int? = savedStateHandleProvider.get()
        .get<String>(SecureLinksOverviewMaxViewsNavArgId.key)
        ?.toIntOrNull()

    private val secureLink = savedStateHandleProvider.get()
        .require<String>(SecureLinksOverviewLinkNavArgId.key)
        .let(NavParamEncoder::decode)

    internal val state: StateFlow<SecureLinksOverviewState> = MutableStateFlow(
        SecureLinksOverviewState(
            secureLink = secureLink,
            expiration = expiration,
            maxViewsAllows = maxViewsAllowed
        )
    )

    internal fun onLinkCopied() {
        clipboardManager.copyToClipboard(text = secureLink, isSecure = false)

        viewModelScope.launch {
            snackbarDispatcher(SecureLinksOverviewSnackbarMessage.LinkCopied)
        }
    }

}
