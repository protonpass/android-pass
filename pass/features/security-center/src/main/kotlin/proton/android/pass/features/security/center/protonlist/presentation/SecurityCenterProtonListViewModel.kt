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

package proton.android.pass.features.security.center.protonlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.breach.ObserveAllBreachByUserId
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import javax.inject.Inject

@HiltViewModel
class SecurityCenterProtonListViewModel @Inject constructor(
    observeAllBreachByUserId: ObserveAllBreachByUserId,
    observeGlobalMonitorState: ObserveGlobalMonitorState
) : ViewModel() {

    private val protonEmailFlow = observeAllBreachByUserId()
        .map { breach -> breach.breachedProtonEmails }
        .asLoadingResult()

    private val eventFlow =
        MutableStateFlow<SecurityCenterProtonListEvent>(SecurityCenterProtonListEvent.Idle)

    internal val state: StateFlow<SecurityCenterProtonListState> = combine(
        observeGlobalMonitorState(),
        protonEmailFlow,
        eventFlow
    ) { monitorState, protonEmailsResult, event ->
        val listState = when (protonEmailsResult) {
            is LoadingResult.Error -> ProtonListState.Error(ProtonListError.CannotLoad)
            LoadingResult.Loading -> ProtonListState.Loading
            is LoadingResult.Success -> {
                val (excluded, included) = if (!monitorState.protonMonitorEnabled) {
                    protonEmailsResult.data.map { it.copy(flags = 0) }
                } else {
                    protonEmailsResult.data
                }.partition { it.isMonitoringDisabled }

                ProtonListState.Success(
                    includedEmails = included.toBreachRowState(),
                    excludedEmails = excluded.toBreachRowState()
                )
            }
        }
        SecurityCenterProtonListState(
            isGlobalMonitorEnabled = monitorState.protonMonitorEnabled,
            listState = listState,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterProtonListState.Initial
    )

    private fun List<BreachProtonEmail>?.toBreachRowState(): ImmutableList<EmailBreachUiState> = if (this != null) {
        map { breachProtonEmail ->
            EmailBreachUiState(
                id = BreachEmailId.Proton(
                    id = BreachId(breachProtonEmail.addressId.id),
                    breachProtonEmail.addressId
                ),
                email = breachProtonEmail.email,
                count = breachProtonEmail.breachCounter,
                breachDate = breachProtonEmail.lastBreachTime
                    ?.let { DateUtils.formatDate(it) }
                    ?.getOrNull(),
                isMonitored = !breachProtonEmail.isMonitoringDisabled
            )
        }.toPersistentList()
    } else {
        persistentListOf()
    }

    internal fun onEventConsumed(event: SecurityCenterProtonListEvent) {
        eventFlow.compareAndSet(event, SecurityCenterProtonListEvent.Idle)
    }
}

