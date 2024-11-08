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

package proton.android.pass.features.sl.sync.management.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasDomains
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasSettings
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncManagementViewModel @Inject constructor(
    observeSimpleLoginAliasDomains: ObserveSimpleLoginAliasDomains,
    observeSimpleLoginAliasMailboxes: ObserveSimpleLoginAliasMailboxes,
    observeSimpleLoginAliasSettings: ObserveSimpleLoginAliasSettings,
    observeSimpleLoginSyncStatus: ObserveSimpleLoginSyncStatus,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val modelOptionFlow = combine(
        observeSimpleLoginAliasDomains(),
        observeSimpleLoginAliasMailboxes(),
        observeSimpleLoginAliasSettings(),
        observeSimpleLoginSyncStatus()
    ) { aliasDomains, aliasMailboxes, aliasSettings, syncStatus ->
        SimpleLoginSyncManagementModel(
            aliasDomains = aliasDomains,
            aliasMailboxes = aliasMailboxes,
            aliasSettings = aliasSettings,
            syncStatus = syncStatus
        ).some()
    }.catch { error ->
        PassLogger.w(TAG, "There was an error while observing SL alias details")
        PassLogger.w(TAG, error)
        snackbarDispatcher(SimpleLoginSyncManagementSnackBarMessage.FetchAliasDetailsError)
        eventFlow.update { SimpleLoginSyncManagementEvent.OnFetchAliasManagementError }
        emit(None)
    }

    private val eventFlow = MutableStateFlow<SimpleLoginSyncManagementEvent>(
        value = SimpleLoginSyncManagementEvent.Idle
    )

    private val isUpdatingFlow = MutableStateFlow(false)

    internal val state: StateFlow<SimpleLoginSyncManagementState> = combine(
        isUpdatingFlow,
        eventFlow,
        modelOptionFlow,
        ::SimpleLoginSyncManagementState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SimpleLoginSyncManagementState.Initial
    )

    internal fun onConsumeEvent(event: SimpleLoginSyncManagementEvent) {
        eventFlow.compareAndSet(event, SimpleLoginSyncManagementEvent.Idle)
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncDetailsViewModel"

    }

}
