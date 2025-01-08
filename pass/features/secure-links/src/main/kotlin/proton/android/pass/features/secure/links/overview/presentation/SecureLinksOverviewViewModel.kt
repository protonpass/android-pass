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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.securelink.DeleteSecureLink
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLink
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksLinkIdNavArgId
import proton.android.pass.features.secure.links.shared.presentation.SecureLinksSharedSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SecureLinksOverviewViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSecureLink: ObserveSecureLink,
    observeItemById: ObserveItemById,
    observeShare: ObserveShare,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
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

    private val itemUiModelFlow = secureLinkFlow.flatMapLatest { secureLink ->
        observeItemById(shareId = secureLink.shareId, itemId = secureLink.itemId)
    }.map { item ->
        encryptionContextProvider.withEncryptionContext {
            item.toUiModel(this@withEncryptionContext).copy(isPinned = false)
        }
    }

    private val vaultOptionFlow = secureLinkFlow
        .flatMapLatest { secureLink -> observeShare(shareId = secureLink.shareId) }
        .mapLatest { share -> share.toVault() }

    private val eventFlow = MutableStateFlow<SecureLinksOverviewEvent>(SecureLinksOverviewEvent.Idle)

    private val isDeletingLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val state: StateFlow<SecureLinksOverviewState> = combineN(
        secureLinkFlow,
        itemUiModelFlow,
        vaultOptionFlow,
        userPreferencesRepository.getUseFaviconsPreference(),
        eventFlow,
        isDeletingLoadingStateFlow
    ) { secureLink, itemUiModel, vaultOption, useFavIconsPreference, event, isDeletingLoadingState ->
        SecureLinksOverviewState(
            secureLinkUrl = secureLink.url,
            currentViews = secureLink.readCount,
            expirationSeconds = secureLink.expirationInSeconds,
            maxViewsAllowed = secureLink.maxReadCount,
            itemUiModel = itemUiModel,
            canLoadExternalImages = useFavIconsPreference.value(),
            vaultOption = vaultOption,
            event = event,
            isDeletingLoadingState = isDeletingLoadingState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SecureLinksOverviewState.Initial
    )

    internal fun onEventConsumed(event: SecureLinksOverviewEvent) {
        eventFlow.compareAndSet(event, SecureLinksOverviewEvent.Idle)
    }

    internal fun onCopyLink() {
        clipboardManager.copyToClipboard(text = state.value.secureLinkUrl, isSecure = false)

        viewModelScope.launch {
            snackbarDispatcher(SecureLinksSharedSnackbarMessage.LinkCopied)
        }
    }

    internal fun onDeletedLink() {
        viewModelScope.launch {
            isDeletingLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { deleteSecureLink(secureLinkId) }
                .onError { error ->
                    PassLogger.w(TAG, "There was an error deleting the secure link")
                    PassLogger.w(TAG, error)
                    if (error is CancellationException) {
                        SecureLinksSharedSnackbarMessage.LinkDeletionCanceled
                    } else {
                        SecureLinksSharedSnackbarMessage.LinkDeletionError
                    }.also { snackbarMessage -> snackbarDispatcher(snackbarMessage) }

                    eventFlow.update { SecureLinksOverviewEvent.OnDeleteLinkError }
                }
                .onSuccess {
                    eventFlow.update { SecureLinksOverviewEvent.OnLinkDeleted }
                }

            isDeletingLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "SecureLinksOverviewViewModel"

    }

}
