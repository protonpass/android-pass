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

package proton.android.pass.features.security.center.addressoptions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.breach.UpdateGlobalAliasAddressesMonitorState
import proton.android.pass.data.api.usecases.breach.UpdateGlobalProtonAddressesMonitorState
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsTypeArgId
import proton.android.pass.features.security.center.addressoptions.navigation.AddressType
import proton.android.pass.features.security.center.addressoptions.navigation.AddressTypeArgId
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterAddressOptionsEvent
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.DisableMonitoringError
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.EnableMonitoringError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecurityCenterAddressOptionsViewModel @Inject constructor(
    private val updateProtonAddressesMonitorState: UpdateGlobalProtonAddressesMonitorState,
    private val updateAliasAddressesMonitorState: UpdateGlobalAliasAddressesMonitorState,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val addressOptionsType: AddressOptionsType = savedStateHandleProvider.get()
        .require(AddressOptionsTypeArgId.key)
    private val addressType: AddressType = savedStateHandleProvider.get()
        .require(AddressTypeArgId.key)

    private val eventFlow =
        MutableStateFlow<SecurityCenterAddressOptionsEvent>(SecurityCenterAddressOptionsEvent.Idle)

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    internal val state: StateFlow<SecurityCenterAddressOptionsState> = combine(
        flowOf(addressOptionsType),
        eventFlow,
        isLoadingState,
        ::SecurityCenterAddressOptionsState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SecurityCenterAddressOptionsState.Initial
    )

    internal fun onEventConsumed(event: SecurityCenterAddressOptionsEvent) {
        eventFlow.compareAndSet(event, SecurityCenterAddressOptionsEvent.Idle)
    }

    fun updateMonitorState(enabled: Boolean) {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                when (addressType) {
                    AddressType.Proton ->
                        updateProtonAddressesMonitorState(enabled = enabled)

                    AddressType.Alias ->
                        updateAliasAddressesMonitorState(enabled = enabled)
                }
            }
                .onSuccess {
                    eventFlow.update { SecurityCenterAddressOptionsEvent.OnMonitorStateUpdated }
                }
                .onFailure {
                    PassLogger.w(TAG, "Failed to update monitor state for $addressType")
                    PassLogger.w(TAG, it)
                    if (enabled) {
                        snackbarDispatcher(EnableMonitoringError)
                    } else {
                        snackbarDispatcher(DisableMonitoringError)
                    }
                }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    companion object {
        private const val TAG = "SecurityCenterAddressOptionsViewModel"
    }
}
