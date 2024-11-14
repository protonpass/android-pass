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

package proton.android.pass.features.sharing.sharingsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.features.sharing.SharingSnackbarMessage.InviteSentSuccess
import proton.android.pass.features.sharing.SharingSnackbarMessage.VaultNotFound
import proton.android.pass.features.sharing.common.toUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SharingSummaryViewModel @Inject constructor(
    private val inviteToVault: InviteToVault,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val bulkInviteRepository: BulkInviteRepository,
    private val getUserPlan: GetUserPlan,
    getVaultWithItemCountById: GetVaultWithItemCountById,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val eventFlow: MutableStateFlow<SharingSummaryEvent> =
        MutableStateFlow(SharingSummaryEvent.Unknown)

    private val addressesFlow: StateFlow<List<AddressPermission>> = bulkInviteRepository
        .observeAddresses()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    internal val state: StateFlow<SharingSummaryUIState> = combine(
        addressesFlow,
        getVaultWithItemCountById(shareId = shareId).asLoadingResult(),
        isLoadingStateFlow,
        eventFlow
    ) { addresses, vaultResult, isLoadingState, event ->
        var uiEvent = if (event == SharingSummaryEvent.Unknown && addresses.isEmpty()) {
            SharingSummaryEvent.BackToHome
        } else {
            event
        }

        val vaultWithItemCount = when (vaultResult) {
            is LoadingResult.Success -> vaultResult.data
            is LoadingResult.Error -> {
                snackbarDispatcher(VaultNotFound)
                uiEvent = SharingSummaryEvent.BackToHome
                null
            }

            is LoadingResult.Loading -> null
        }
        val isLoading = vaultResult is LoadingResult.Loading ||
            isLoadingState is IsLoadingState.Loading
        SharingSummaryUIState(
            addresses = addresses.map { it.toUiState() }.toPersistentList(),
            vaultWithItemCount = vaultWithItemCount,
            isLoading = isLoading,
            event = uiEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingSummaryUIState()
    )

    fun onSubmit() = viewModelScope.launch {
        isLoadingStateFlow.update { IsLoadingState.Loading }
        inviteToVault(
            inviteAddresses = addressesFlow.value,
            shareId = shareId
        ).onSuccess {
            bulkInviteRepository.clear()
            isLoadingStateFlow.update { IsLoadingState.NotLoading }
            snackbarDispatcher(InviteSentSuccess)
            PassLogger.i(TAG, "Invite sent successfully")
            eventFlow.update { SharingSummaryEvent.Shared }
        }.onFailure { error ->
            isLoadingStateFlow.update { IsLoadingState.NotLoading }

            if (getUserPlan().first().isBusinessPlan) {
                eventFlow.update { SharingSummaryEvent.Error }
            } else {
                snackbarDispatcher(SharingSnackbarMessage.InviteSentError)
            }

            PassLogger.w(TAG, "Error sending invite")
            PassLogger.w(TAG, error)
        }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { SharingSummaryEvent.Unknown }
    }

    companion object {
        private const val TAG = "SharingSummaryViewModel"
    }
}
