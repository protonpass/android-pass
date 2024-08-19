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

package proton.android.pass.features.sl.sync.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasDomains
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncDetailsViewModel @Inject constructor(
    observeSimpleLoginAliasDomains: ObserveSimpleLoginAliasDomains,
    observeSimpleLoginAliasMailboxes: ObserveSimpleLoginAliasMailboxes,
    observeSimpleLoginSyncStatus: ObserveSimpleLoginSyncStatus
) : ViewModel() {

    private val aliasDomainsFlow = observeSimpleLoginAliasDomains()

    private val aliasMailboxesFlow = observeSimpleLoginAliasMailboxes()

    private val selectedDomainOptionFlow = MutableStateFlow<Option<String>>(None)

    private val selectedMailboxIdOptionFlow = MutableStateFlow<Option<String>>(None)

    internal val state: StateFlow<SimpleLoginSyncDetailsState> = combine(
        aliasDomainsFlow,
        aliasMailboxesFlow,
        observeSimpleLoginSyncStatus(),
        selectedDomainOptionFlow,
        selectedMailboxIdOptionFlow
    ) { aliasDomains, aliasMailboxes, syncStatus, selectedDomainOption, selectedMailboxIdOption ->
        SimpleLoginSyncDetailsState(
            aliasDomains = aliasDomains,
            aliasMailboxes = aliasMailboxes,
            defaultVaultOption = syncStatus.value()?.defaultVault.toOption(),
            pendingAliasesCountOption = syncStatus.value()?.pendingAliasCount.toOption(),
            isLoading = false,
            selectedDomainOption = selectedDomainOption,
            selectedMailboxIdOption = selectedMailboxIdOption

        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SimpleLoginSyncDetailsState.Initial
    )

    internal fun onSelectAliasDomain(selectedAliasDomain: SimpleLoginAliasDomain) {
        selectedDomainOptionFlow.update { selectedAliasDomain.domain.some() }
    }

    internal fun onSelectAliasMailbox(selectedAliasMailbox: SimpleLoginAliasMailbox) {
        selectedMailboxIdOptionFlow.update { selectedAliasMailbox.id.some() }
    }

}
