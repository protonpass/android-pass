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

package proton.android.pass.features.security.center.aliaslist.presentation

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
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.breach.ObserveBreachAliasEmails
import proton.android.pass.domain.breach.AliasData
import proton.android.pass.domain.breach.AliasKeyId
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.PassMonitorDisplayMonitoringEmailAliases
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.IsDarkWebAliasMessageDismissedPreference
import proton.android.pass.preferences.IsDarkWebAliasMessageDismissedPreference.Show
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class SecurityCenterAliasListViewModel @Inject constructor(
    observeBreachAliasEmails: ObserveBreachAliasEmails,
    observeGlobalMonitorState: ObserveGlobalMonitorState,
    telemetryManager: TelemetryManager,
    private val internalSettingsRepository: InternalSettingsRepository
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayMonitoringEmailAliases)
    }

    private val eventFlow =
        MutableStateFlow<SecurityCenterAliasListEvent>(SecurityCenterAliasListEvent.Idle)

    internal val state: StateFlow<SecurityCenterAliasListState> = combine(
        observeGlobalMonitorState().asLoadingResult(),
        observeBreachAliasEmails().asLoadingResult(),
        internalSettingsRepository.getDarkWebAliasMessageVisibility(),
        eventFlow
    ) { monitorState,
        aliasEmail,
        darkWebAliasMessageVisibility,
        event ->
        val isGlobalAliasMonitorEnabled = monitorState.getOrNull()?.aliasMonitorEnabled ?: true

        val isLoading = monitorState is LoadingResult.Loading ||
            aliasEmail is LoadingResult.Loading

        val aliasIncludedWithoutBreachesList = when (aliasEmail) {
            is LoadingResult.Error,
            LoadingResult.Loading -> emptyMap()

            is LoadingResult.Success -> aliasEmail.data.filter { it.value.isMonitored && it.value.breaches.isEmpty() }
        }
        val aliasIncludedWithBreachesList = when (aliasEmail) {
            is LoadingResult.Error,
            LoadingResult.Loading -> emptyMap()

            is LoadingResult.Success -> aliasEmail.data.filter {
                it.value.isMonitored && it.value.breaches.isNotEmpty()
            }
        }
        val aliasExcludedEmailsList = when (aliasEmail) {
            is LoadingResult.Error,
            LoadingResult.Loading -> emptyMap()

            is LoadingResult.Success -> aliasEmail.data.filter { !it.value.isMonitored }
        }
        val listState = when {
            isLoading -> AliasListState.Loading
            !isGlobalAliasMonitorEnabled -> AliasListState.Success(
                includedBreachedEmails = persistentListOf(),
                includedMonitoredEmails = persistentListOf(),
                excludedEmails = (
                    aliasIncludedWithBreachesList.toEmailBreachUiState()
                        .map { it.copy(isMonitored = false) } +
                        aliasExcludedEmailsList.toEmailBreachUiState()
                            .map { it.copy(isMonitored = false) } +
                        aliasIncludedWithoutBreachesList.toEmailBreachUiState()
                            .map { it.copy(isMonitored = false) }
                    ).toPersistentList()
            )

            else -> AliasListState.Success(
                includedBreachedEmails = aliasIncludedWithBreachesList.toEmailBreachUiState(),
                includedMonitoredEmails = aliasIncludedWithoutBreachesList.toEmailBreachUiState(),
                excludedEmails = aliasExcludedEmailsList.toEmailBreachUiState()
            )
        }
        SecurityCenterAliasListState(
            isGlobalMonitorEnabled = isGlobalAliasMonitorEnabled,
            isCustomEmailMessageEnabled = darkWebAliasMessageVisibility == Show,
            listState = listState,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterAliasListState.Initial
    )

    private fun Map<AliasKeyId, AliasData>.toEmailBreachUiState(): ImmutableList<EmailBreachUiState> = map { entry ->
        val breachDate = entry.value.breaches.firstOrNull()
            ?.publishedAt
            ?.let(DateUtils::formatDate)
            ?.getOrNull()
        EmailBreachUiState(
            id = BreachEmailId.Alias(
                id = BreachId(""),
                shareId = entry.key.shareId,
                itemId = entry.key.itemId
            ),
            email = entry.key.alias,
            count = entry.value.breaches.count(),
            breachDate = breachDate,
            isMonitored = entry.value.isMonitored
        )
    }.toPersistentList()

    internal fun onEventConsumed(event: SecurityCenterAliasListEvent) {
        eventFlow.compareAndSet(event, SecurityCenterAliasListEvent.Idle)
    }

    internal fun dismissCustomEmailMessage() {
        internalSettingsRepository.setDarkWebAliasMessageVisibility(
            IsDarkWebAliasMessageDismissedPreference.Dismissed
        )
    }
}
