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

package proton.android.pass.features.secure.links.listmenu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.securelink.DeleteSecureLink
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksLinkIdNavArgId
import proton.android.pass.features.secure.links.shared.presentation.SecureLinksSharedSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SecureLinksListMenuViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSecureLink: ObserveSecureLink,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val deleteSecureLink: DeleteSecureLink
) : ViewModel() {

    private val secureLinkId: SecureLinkId = savedStateHandleProvider.get()
        .require<String>(SecureLinksLinkIdNavArgId.key)
        .let(::SecureLinkId)

    private val secureLinkFlow = oneShot {
        observeSecureLink(secureLinkId = secureLinkId).first()
    }

    private val eventFlow = MutableStateFlow<SecureLinksListMenuEvent>(SecureLinksListMenuEvent.Idle)

    internal val state: StateFlow<SecureLinksListMenuState> = combine(
        secureLinkFlow,
        eventFlow
    ) { secureLink, event ->
        SecureLinksListMenuState(
            secureLinkUrl = secureLink.url,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecureLinksListMenuState.Initial
    )

    internal fun onEventConsumed(event: SecureLinksListMenuEvent) {
        eventFlow.compareAndSet(event, SecureLinksListMenuEvent.Idle)
    }

    internal fun onCopyLink() {
        clipboardManager.copyToClipboard(text = state.value.secureLinkUrl, isSecure = false)

        viewModelScope.launch {
            snackbarDispatcher(SecureLinksSharedSnackbarMessage.LinkCopied)
        }

        eventFlow.update { SecureLinksListMenuEvent.OnLinkCopied }
    }

    internal fun onDeletedLink() {
        viewModelScope.launch {
            runCatching { deleteSecureLink(secureLinkId) }
                .onError { error ->
                    PassLogger.w(TAG, "There was an error deleting the secure link")
                    PassLogger.w(TAG, error)
                    if (error is CancellationException) {
                        SecureLinksSharedSnackbarMessage.LinkDeletionCanceled
                    } else {
                        SecureLinksSharedSnackbarMessage.LinkDeletionError
                    }.also { snackbarMessage -> snackbarDispatcher(snackbarMessage) }

                    eventFlow.update { SecureLinksListMenuEvent.OnDeleteLinkError }
                }
                .onSuccess {
                    eventFlow.update { SecureLinksListMenuEvent.OnLinkDeleted }
                }
        }
    }

    private companion object {

        private const val TAG = "SecureLinksListMenuViewModel"

    }

}
