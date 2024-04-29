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
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.breach.RemoveCustomEmail
import proton.android.pass.data.api.usecases.breach.UpdateGlobalAliasAddressesMonitorState
import proton.android.pass.data.api.usecases.breach.UpdateGlobalProtonAddressesMonitorState
import proton.android.pass.data.api.usecases.breach.UpdateProtonAddressMonitorState
import proton.android.pass.data.api.usecases.items.UpdateItemFlag
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsTypeArgId
import proton.android.pass.features.security.center.addressoptions.navigation.AddressTypeArgId
import proton.android.pass.features.security.center.addressoptions.navigation.GlobalMonitorAddressType
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterAddressOptionsEvent
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.DisableMonitoringError
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.DisableMonitoringSuccess
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.EnableMonitoringError
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.EnableMonitoringSuccess
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.RemoveCustomEmailError
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsSnackbarMessage.RemoveCustomEmailSuccess
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecurityCenterAddressOptionsViewModel @Inject constructor(
    private val updateGlobalProtonAddressesMonitorState: UpdateGlobalProtonAddressesMonitorState,
    private val updateGlobalAliasAddressesMonitorState: UpdateGlobalAliasAddressesMonitorState,
    private val updateProtonAddressMonitorState: UpdateProtonAddressMonitorState,
    private val removeCustomEmail: RemoveCustomEmail,
    private val updateItemFlag: UpdateItemFlag,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val addressOptionsType: AddressOptionsType = savedStateHandleProvider.get()
        .require(AddressOptionsTypeArgId.key)
    private val globalMonitorAddressType: GlobalMonitorAddressType = savedStateHandleProvider.get()
        .get<GlobalMonitorAddressType>(AddressTypeArgId.key)
        ?: GlobalMonitorAddressType.None

    private val breachCustomEmailId: BreachEmailId.Custom? = savedStateHandleProvider.get()
        .get<String>(BreachIdArgId.key)
        ?.let { BreachEmailId.Custom(BreachId(""), CustomEmailId(it)) }

    private val aliasEmailId: BreachEmailId.Alias? = run {
        val shareId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ShareId.key)
            ?.let(::ShareId)
        val itemId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ItemId.key)
            ?.let(::ItemId)
        if (shareId != null && itemId != null) {
            BreachEmailId.Alias(BreachId(""), shareId, itemId)
        } else {
            null
        }
    }

    private val protonEmailId: BreachEmailId.Proton? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.AddressId.key)
        ?.let { BreachEmailId.Proton(BreachId(""), AddressId(it)) }

    private val breachEmailId: BreachEmailId? = when {
        protonEmailId != null -> protonEmailId
        aliasEmailId != null -> aliasEmailId
        breachCustomEmailId != null -> breachCustomEmailId
        else -> null
    }

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
                when (globalMonitorAddressType) {
                    GlobalMonitorAddressType.Proton ->
                        updateGlobalProtonAddressesMonitorState(enabled = enabled)

                    GlobalMonitorAddressType.Alias ->
                        updateGlobalAliasAddressesMonitorState(enabled = enabled)

                    GlobalMonitorAddressType.None ->
                        when (breachEmailId) {
                            is BreachEmailId.Proton -> updateProtonAddressMonitorState(
                                addressId = breachEmailId.addressId,
                                enabled = enabled
                            )

                            is BreachEmailId.Alias -> updateItemFlag(
                                shareId = breachEmailId.shareId,
                                itemId = breachEmailId.itemId,
                                flag = ItemFlag.SkipHealthCheck,
                                isFlagEnabled = !enabled
                            )

                            else -> throw IllegalStateException("Invalid state")
                        }
                }
            }
                .onSuccess {
                    PassLogger.i(TAG, "Monitor state updated $enabled")
                    eventFlow.update { SecurityCenterAddressOptionsEvent.OnMonitorStateUpdated }
                    if (enabled) {
                        snackbarDispatcher(EnableMonitoringSuccess)
                    } else {
                        snackbarDispatcher(DisableMonitoringSuccess)
                    }
                }
                .onFailure {
                    PassLogger.w(
                        TAG,
                        "Failed to update monitor state for $globalMonitorAddressType"
                    )
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

    fun removeCustomEmailClick() {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                val id =
                    breachCustomEmailId?.customEmailId
                        ?: throw IllegalStateException("Invalid state")
                removeCustomEmail(id = id)
            }
                .onSuccess {
                    PassLogger.i(TAG, "Custom email removed")
                    eventFlow.update { SecurityCenterAddressOptionsEvent.OnCustomEmailRemoved }
                    snackbarDispatcher(RemoveCustomEmailSuccess)
                }
                .onFailure {
                    PassLogger.w(TAG, "Failed to remove custom email")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(RemoveCustomEmailError)
                }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    companion object {
        private const val TAG = "SecurityCenterAddressOptionsViewModel"
    }
}
