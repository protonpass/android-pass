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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import javax.inject.Inject

@HiltViewModel
class SecurityCenterAliasListViewModel @Inject constructor(
    observeItems: ObserveItems,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeGlobalMonitorState: ObserveGlobalMonitorState
) : ViewModel() {

    private val aliasIncludedWithoutBreachesFlow = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Aliases,
        securityCheckFilter = ItemSecurityCheckFilter.Included,
        isBreachedFilter = ItemIsBreachedFilter.NotBreached
    ).asLoadingResult()

    private val aliasIncludedWithBreachesFlow = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Aliases,
        securityCheckFilter = ItemSecurityCheckFilter.Included,
        isBreachedFilter = ItemIsBreachedFilter.Breached
    )
        .flatMapLatest { items ->
            if (items.isEmpty()) {
                flowOf(emptyMap())
            } else {
                items.map { item ->
                    observeBreachesForAliasEmail(
                        shareId = item.shareId,
                        itemId = item.id
                    )
                }.merge().map { list -> list.groupBy { it.email } }
            }
        }
        .asLoadingResult()

    private val aliasExcludedEmailFlow = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Aliases,
        securityCheckFilter = ItemSecurityCheckFilter.Excluded
    ).asLoadingResult()

    private val eventFlow =
        MutableStateFlow<SecurityCenterAliasListEvent>(SecurityCenterAliasListEvent.Idle)

    internal val state: StateFlow<SecurityCenterAliasListState> = combine(
        observeGlobalMonitorState().asLoadingResult(),
        aliasIncludedWithoutBreachesFlow,
        aliasIncludedWithBreachesFlow,
        aliasExcludedEmailFlow,
        eventFlow
    ) { monitorState,
        aliasIncludedWithoutBreaches, aliasIncludedWithBreaches, aliasExcluded, event ->
        val isGlobalAliasMonitorEnabled = monitorState.getOrNull()?.aliasMonitorEnabled ?: true

        val isLoading = monitorState is LoadingResult.Loading ||
            aliasIncludedWithoutBreaches is LoadingResult.Loading ||
            aliasIncludedWithBreaches is LoadingResult.Loading ||
            aliasExcluded is LoadingResult.Loading

        val aliasIncludedWithoutBreachesList = when (aliasIncludedWithoutBreaches) {
            is LoadingResult.Error -> emptyList()
            LoadingResult.Loading -> emptyList()
            is LoadingResult.Success -> aliasIncludedWithoutBreaches.data
        }
        val aliasIncludedWithBreachesList = when (aliasIncludedWithBreaches) {
            is LoadingResult.Error -> emptyMap()
            LoadingResult.Loading -> emptyMap()
            is LoadingResult.Success -> aliasIncludedWithBreaches.data
        }
        val aliasExcludedEmailsList = when (aliasExcluded) {
            is LoadingResult.Error -> emptyList()
            LoadingResult.Loading -> emptyList()
            is LoadingResult.Success -> aliasExcluded.data
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
            listState = listState,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterAliasListState.Initial
    )

    private fun List<Item>.toEmailBreachUiState(): ImmutableList<EmailBreachUiState> = map { item ->
        EmailBreachUiState(
            id = BreachEmailId.Alias(
                id = BreachId(id = ""),
                shareId = item.shareId,
                itemId = item.id
            ),
            email = (item.itemType as ItemType.Alias).aliasEmail,
            count = 0,
            breachDate = null,
            isMonitored = false
        )
    }.toPersistentList()

    private fun Map<String, List<BreachEmail>>.toEmailBreachUiState(): ImmutableList<EmailBreachUiState> =
        map { entry ->
            val id = entry.value.first().emailId as BreachEmailId.Alias
            val breachDate = entry.value.first().publishedAt.let(DateUtils::formatDate).getOrNull()
            EmailBreachUiState(
                id = BreachEmailId.Alias(
                    id = id.id,
                    shareId = id.shareId,
                    itemId = id.itemId
                ),
                email = entry.key,
                count = entry.value.count(),
                breachDate = breachDate,
                isMonitored = true
            )
        }.toPersistentList()

    internal fun onEventConsumed(event: SecurityCenterAliasListEvent) {
        eventFlow.compareAndSet(event, SecurityCenterAliasListEvent.Idle)
    }
}

