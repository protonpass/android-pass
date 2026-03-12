/*
 * Copyright (c) 2024-2026 Proton AG
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.common.api.some
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasDomains
import proton.android.pass.data.api.usecases.simplelogin.UpdateSimpleLoginAliasDomain
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncDomainSelectViewModel @Inject constructor(
    observeSimpleLoginAliasDomains: ObserveSimpleLoginAliasDomains,
    getUserPlan: GetUserPlan,
    private val updateSimpleLoginAliasDomain: UpdateSimpleLoginAliasDomain,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val canSelectPremiumDomainsFlow = getUserPlan()
        .map { userPlan -> userPlan.isPaidPlan }
        .onStart { emit(false) }
        .catch { error ->
            PassLogger.w(TAG, "There was an error while observing the user plan")
            PassLogger.w(TAG, error)
            emit(false)
        }

    private val eventFlow = MutableStateFlow<SimpleLoginSyncDomainSelectEvent>(
        value = SimpleLoginSyncDomainSelectEvent.Idle
    )

    private val updatingAliasDomainOptionFlow = MutableStateFlow<Option<String>>(None)

    private val aliasDomainsFlow = observeSimpleLoginAliasDomains()
        .catch { error ->
            PassLogger.w(TAG, "There was an error while observing SL alias domains")
            PassLogger.w(TAG, error)
            eventFlow.update { SimpleLoginSyncDomainSelectEvent.OnFetchAliasDomainsError }
            snackbarDispatcher(SimpleLoginSyncDomainSelectSnackBarMessage.FetchAliasDomainError)
            emit(emptyList())
        }

    internal val stateFlow: StateFlow<SimpleLoginSyncDomainSelectState> = combine(
        canSelectPremiumDomainsFlow,
        aliasDomainsFlow,
        eventFlow,
        updatingAliasDomainOptionFlow
    ) { canSelectPremiumDomains, aliasDomains, event, updatingAliasDomainOption ->
        SimpleLoginSyncDomainSelectState(
            canSelectPremiumDomains = canSelectPremiumDomains,
            simpleLoginAliasDomains = aliasDomains,
            event = event,
            updatingAliasDomainOption = updatingAliasDomainOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncDomainSelectState.Initial
    )

    internal fun onUpdateAliasDomain(selectedAliasDomain: String) {
        viewModelScope.launch {
            updatingAliasDomainOptionFlow.update { selectedAliasDomain.some() }

            runCatching { updateSimpleLoginAliasDomain(domain = selectedAliasDomain) }
                .onError { error ->
                    PassLogger.w(TAG, "There was an error updating SL alias domain")
                    PassLogger.w(TAG, error)
                    eventFlow.update { SimpleLoginSyncDomainSelectEvent.OnUpdateAliasDomainError }
                    snackbarDispatcher(SimpleLoginSyncDomainSelectSnackBarMessage.UpdateAliasDomainError)
                }
                .onSuccess {
                    eventFlow.update { SimpleLoginSyncDomainSelectEvent.OnUpdateAliasDomainSuccess }
                    snackbarDispatcher(SimpleLoginSyncDomainSelectSnackBarMessage.UpdateAliasDomainSuccess)
                }

            updatingAliasDomainOptionFlow.update { None }
        }
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncDetailsViewModel"

    }

}
