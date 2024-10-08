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

package proton.android.pass.features.sl.sync.domains.select.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasDomains
import proton.android.pass.data.api.usecases.simplelogin.UpdateSimpleLoginAliasDomain
import proton.android.pass.features.sl.sync.domains.select.navigation.SimpleLoginSyncDomainSelectPremiumNavId
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementSnackBarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncDomainSelectViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSimpleLoginAliasDomains: ObserveSimpleLoginAliasDomains,
    private val updateSimpleLoginAliasDomain: UpdateSimpleLoginAliasDomain,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val canSelectPremiumDomains = savedStateHandleProvider.get()
        .require<Boolean>(SimpleLoginSyncDomainSelectPremiumNavId.key)

    private val eventFlow = MutableStateFlow<SimpleLoginSyncDomainSelectEvent>(
        value = SimpleLoginSyncDomainSelectEvent.Idle
    )

    internal val stateFlow: StateFlow<SimpleLoginSyncDomainSelectState> = combine(
        observeSimpleLoginAliasDomains(),
        eventFlow
    ) { aliasDomains, event ->
        SimpleLoginSyncDomainSelectState(
            canSelectPremiumDomains = canSelectPremiumDomains,
            simpleLoginAliasDomains = aliasDomains,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncDomainSelectState.Initial
    )

    internal fun onUpdateAliasDomain(selectedAliasDomain: String) {
        viewModelScope.launch {

            runCatching {
                updateSimpleLoginAliasDomain(domain = selectedAliasDomain.takeIf { it.isNotEmpty() })
            }.onError { error ->
                PassLogger.w(TAG, "There was an error updating SL alias domain")
                PassLogger.w(TAG, error)
                eventFlow.update { SimpleLoginSyncDomainSelectEvent.OnUpdateAliasDomainError }
                snackbarDispatcher(SimpleLoginSyncManagementSnackBarMessage.UpdateAliasDomainError)
            }.onSuccess {
                eventFlow.update { SimpleLoginSyncDomainSelectEvent.OnUpdateAliasDomainSuccess }
                snackbarDispatcher(SimpleLoginSyncManagementSnackBarMessage.UpdateAliasDomainSuccess)
            }

        }
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncDetailsViewModel"

    }

}
