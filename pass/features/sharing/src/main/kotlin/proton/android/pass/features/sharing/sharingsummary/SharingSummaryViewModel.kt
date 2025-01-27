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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.FreeUserInviteError
import proton.android.pass.data.api.errors.NewUsersInviteError
import proton.android.pass.data.api.errors.UserAlreadyInviteError
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.invites.InviteToItem
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SharingSummaryViewModel @Inject constructor(
    private val inviteToVault: InviteToVault,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val bulkInviteRepository: BulkInviteRepository,
    private val getUserPlan: GetUserPlan,
    private val inviteToItem: InviteToItem,
    getVaultWithItemCountById: GetVaultWithItemCountById,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItemById: ObserveItemById,
    encryptionContextProvider: EncryptionContextProvider,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemIdOption: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        .toOption()
        .map(::ItemId)

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    private val eventFlow: MutableStateFlow<SharingSummaryEvent> = MutableStateFlow(
        value = SharingSummaryEvent.Idle
    )

    private val addressesFlow: Flow<List<AddressPermission>> = bulkInviteRepository
        .observeAddresses()
        .onEach { addressPermissions ->
            if (addressPermissions.isEmpty()) {
                eventFlow.update { SharingSummaryEvent.OnGoHome }
            }
        }
        .distinctUntilChanged()

    internal val stateFlow: StateFlow<SharingSummaryState> = when (itemIdOption) {
        None -> {
            combine(
                eventFlow,
                addressesFlow,
                isLoadingStateFlow,
                getVaultWithItemCountById(shareId = shareId),
                SharingSummaryState::ShareVault
            )
        }

        is Some -> {
            observeItemById(shareId, itemIdOption.value)
                .mapLatest { item ->
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        item.toUiModel(this@withEncryptionContextSuspendable)
                    }
                }
                .let { itemUiModelFlow ->
                    combine(
                        eventFlow,
                        addressesFlow,
                        isLoadingStateFlow,
                        itemUiModelFlow,
                        userPreferencesRepository.getUseFaviconsPreference(),
                        SharingSummaryState::ShareItem
                    )
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SharingSummaryState.Initial
    )

    internal fun onConsumeEvent(event: SharingSummaryEvent) {
        eventFlow.compareAndSet(event, SharingSummaryEvent.Idle)
    }

    internal fun onShareItem(itemId: ItemId, itemCategory: ItemCategory) {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching {
                inviteToItem(
                    shareId = shareId,
                    itemId = itemId,
                    inviteAddresses = stateFlow.value.addressPermissions
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "Error sending item invite")
                PassLogger.w(TAG, error)

                when (error) {
                    is NewUsersInviteError -> {
                        val invalidAddresses = error.newUsersAddresses.map { it.address }
                        bulkInviteRepository.updateInvalidAddresses(invalidAddresses)
                        eventFlow.update { SharingSummaryEvent.OnSharingItemNewUsersError }
                        SharingSnackbarMessage.NewUsersInviteError
                    }

                    is FreeUserInviteError -> SharingSnackbarMessage.FreeUserInviteError

                    else -> SharingSnackbarMessage.InviteSentError
                }.also { snackbarErrorMessage ->
                    snackbarDispatcher(snackbarErrorMessage)
                }
            }.onSuccess {
                bulkInviteRepository.clear()
                snackbarDispatcher(SharingSnackbarMessage.InviteSentSuccess)
                eventFlow.update { SharingSummaryEvent.OnSharingItemSuccess(itemCategory) }
            }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    internal fun onShareVault() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            inviteToVault(
                inviteAddresses = stateFlow.value.addressPermissions,
                shareId = shareId
            ).onSuccess {
                bulkInviteRepository.clear()
                isLoadingStateFlow.update { IsLoadingState.NotLoading }
                snackbarDispatcher(SharingSnackbarMessage.InviteSentSuccess)
                PassLogger.i(TAG, "Vault invite successfully sent")
                eventFlow.update { SharingSummaryEvent.OnSharingVaultSuccess(shareId) }
            }.onFailure { error ->
                PassLogger.w(TAG, "Error sending vault invite")
                PassLogger.w(TAG, error)

                if (getUserPlan().firstOrNull()?.isBusinessPlan == true) {
                    eventFlow.update { SharingSummaryEvent.OnSharingVaultError }
                    return@onFailure
                }

                if (error is UserAlreadyInviteError) {
                    SharingSnackbarMessage.UserAlreadyInviteError
                } else {
                    SharingSnackbarMessage.InviteSentError
                }.also { snackbarErrorMessage ->
                    snackbarDispatcher(snackbarErrorMessage)
                }
            }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "SharingSummaryViewModel"

    }

}
