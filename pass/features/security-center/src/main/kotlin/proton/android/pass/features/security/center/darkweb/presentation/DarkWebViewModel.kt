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

package proton.android.pass.features.security.center.darkweb.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveAllBreachByUserId
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
internal class DarkWebViewModel @Inject constructor(
    observeItems: ObserveItems,
    observeAllBreachByUserId: ObserveAllBreachByUserId,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeBreachCustomEmails: ObserveBreachCustomEmails,
    observeCustomEmailSuggestions: ObserveCustomEmailSuggestions,
    observeGlobalMonitorState: ObserveGlobalMonitorState
) : ViewModel() {

    private val protonEmailFlow = observeAllBreachByUserId()
        .map { breach -> breach.breachedProtonEmails.filter { it.breachCounter > 0 } }
        .asLoadingResult()

    private val protonEmailFlowIfEnabled = observeGlobalMonitorState()
        .flatMapLatest { monitorState ->
            if (monitorState.protonMonitorEnabled) {
                protonEmailFlow.map { result -> result.map { it to true } }
            } else {
                flowOf(LoadingResult.Success(emptyList<BreachProtonEmail>() to false))
            }
        }

    private val aliasEmailFlow = observeItems(
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

    private val aliasEmailFlowIfEnabled = observeGlobalMonitorState()
        .flatMapLatest { monitorState ->
            if (monitorState.aliasMonitorEnabled) {
                aliasEmailFlow.map { result -> result.map { it to true } }
            } else {
                flowOf(LoadingResult.Success(emptyMap<String, List<BreachEmail>>() to false))
            }
        }

    private val customEmailsFlow: Flow<LoadingResult<List<BreachCustomEmail>>> =
        observeBreachCustomEmails()
            .asLoadingResult()
            .distinctUntilChanged()

    private val customEmailSuggestionsFlow: Flow<LoadingResult<List<CustomEmailSuggestion>>> =
        observeCustomEmailSuggestions()
            .asLoadingResult()
            .distinctUntilChanged()

    val state: StateFlow<DarkWebUiState> = combine(
        protonEmailFlowIfEnabled,
        aliasEmailFlowIfEnabled,
        customEmailsFlow,
        customEmailSuggestionsFlow
    ) { protonEmailResult, aliasEmailsResult, customEmailsResult, suggestionsResult ->

        val protonEmail = getProtonEmailState(protonEmailResult)
        val aliasEmail = getAliasEmailState(aliasEmailsResult)
        val customEmails = getCustomEmailsState(customEmailsResult, suggestionsResult)
        val darkWebStatus = getDarkWebStatus(protonEmail, aliasEmail, customEmails)

        DarkWebUiState(
            protonEmailState = protonEmail,
            aliasEmailState = aliasEmail,
            customEmailState = customEmails,
            darkWebStatus = darkWebStatus,
            lastCheckTime = None
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DarkWebUiState.Initial
    )

    @Suppress("ComplexMethod", "CyclomaticComplexMethod")
    private fun getDarkWebStatus(
        protonEmail: DarkWebEmailBreachState,
        aliasEmail: DarkWebEmailBreachState,
        customEmails: DarkWebCustomEmailsState
    ): DarkWebStatus = when {
        protonEmail is DarkWebEmailBreachState.Error ||
            aliasEmail is DarkWebEmailBreachState.Error ||
            customEmails is DarkWebCustomEmailsState.Error -> {
            PassLogger.w(TAG, "Failed to load dark web data")
            DarkWebStatus.Warning
        }

        protonEmail is DarkWebEmailBreachState.Loading ||
            aliasEmail is DarkWebEmailBreachState.Loading ||
            customEmails is DarkWebCustomEmailsState.Loading -> DarkWebStatus.AllGood

        else -> if (protonEmail is DarkWebEmailBreachState.Success &&
            aliasEmail is DarkWebEmailBreachState.Success &&
            customEmails is DarkWebCustomEmailsState.Success
        ) {
            val noBreaches = protonEmail.emails.all { it.count == 0 } &&
                aliasEmail.emails.all { it.count == 0 } &&
                customEmails.emails.all {
                    when (it.status) {
                        is CustomEmailUiStatus.Verified -> it.status.breachesDetected == 0
                        else -> true
                    }
                }
            if (noBreaches) {
                DarkWebStatus.AllGood
            } else {
                DarkWebStatus.Warning
            }
        } else {
            DarkWebStatus.Loading
        }
    }

    private fun getProtonEmailState(
        protonEmailResult: LoadingResult<Pair<List<BreachProtonEmail>, Boolean>>
    ): DarkWebEmailBreachState = when (protonEmailResult) {
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Failed to load proton emails")
            PassLogger.w(TAG, protonEmailResult.exception)
            DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad)
        }

        LoadingResult.Loading -> DarkWebEmailBreachState.Loading
        is LoadingResult.Success -> DarkWebEmailBreachState.Success(
            protonEmailResult.data.first.map {
                EmailBreachUiState(
                    id = BreachEmailId.Proton(BreachId(it.addressId.id), it.addressId),
                    email = it.email,
                    count = it.breachCounter,
                    breachDate = it.lastBreachTime?.let(DateUtils::formatDate)?.getOrNull(),
                    isMonitored = !it.isMonitoringDisabled
                )
            }.toImmutableList(),
            protonEmailResult.data.second
        )
    }

    private fun getAliasEmailState(
        aliasEmailsResult: LoadingResult<Pair<Map<String, List<BreachEmail>>, Boolean>>
    ): DarkWebEmailBreachState = when (aliasEmailsResult) {
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Failed to load alias emails")
            PassLogger.w(TAG, aliasEmailsResult.exception)
            DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad)
        }

        LoadingResult.Loading -> DarkWebEmailBreachState.Loading
        is LoadingResult.Success -> DarkWebEmailBreachState.Success(
            aliasEmailsResult.data.first.map {
                EmailBreachUiState(
                    id = it.value.first().emailId,
                    email = it.key,
                    count = it.value.size,
                    breachDate = it.value.getLatestBreachDate(),
                    isMonitored = false
                )
            }.toImmutableList(),
            aliasEmailsResult.data.second
        )
    }

    private fun List<BreachEmail>.getLatestBreachDate(): String? = mapNotNull {
        runCatching {
            Instant.parse(it.publishedAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }.getOrNull()
    }.maxOrNull()?.let(DateUtils::formatDate)

    @Suppress("ReturnCount")
    private fun getCustomEmailsState(
        customEmailsResult: LoadingResult<List<BreachCustomEmail>>,
        suggestionsResult: LoadingResult<List<CustomEmailSuggestion>>
    ): DarkWebCustomEmailsState {
        val emails = when (customEmailsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom emails")
                PassLogger.w(TAG, customEmailsResult.exception)
                return DarkWebCustomEmailsState.Error(DarkWebEmailsError.CannotLoad)
            }

            LoadingResult.Loading -> return DarkWebCustomEmailsState.Loading
            is LoadingResult.Success -> customEmailsResult.data.map { it.toUiModel() }
        }

        val (verified, unverified) = emails.partition { it.status is CustomEmailUiStatus.Verified }

        val suggestions = when (suggestionsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom email suggestions")
                PassLogger.w(TAG, suggestionsResult.exception)

                // Return the retrieved emails even if suggestions failed
                return DarkWebCustomEmailsState.Success(
                    emails = emails.toImmutableList(),
                    suggestions = persistentListOf()
                )
            }

            LoadingResult.Loading -> return DarkWebCustomEmailsState.Loading
            is LoadingResult.Success ->
                suggestionsResult.data
                    .filter { it.usedInLoginsCount >= EMAIL_SUGGESTIONS_MIN_USED_IN_COUNT }
                    .map { it.toUiModel() }
        }.take(EMAIL_SUGGESTIONS_COUNT)

        val combined = (verified + unverified).toImmutableList()
        return DarkWebCustomEmailsState.Success(
            emails = combined,
            suggestions = suggestions.toImmutableList()
        )
    }

    private fun CustomEmailSuggestion.toUiModel() = CustomEmailUiState(
        email = email,
        status = CustomEmailUiStatus.Suggestion(usedInLoginsCount)
    )

    private fun BreachCustomEmail.toUiModel(): CustomEmailUiState {
        val status = if (verified) {
            CustomEmailUiStatus.Verified(id, breachCount)
        } else {
            CustomEmailUiStatus.Unverified(id)
        }
        return CustomEmailUiState(
            email = email,
            status = status
        )
    }

    companion object {
        private const val TAG = "DarkWebViewModel"

        private const val EMAIL_SUGGESTIONS_MIN_USED_IN_COUNT = 3
        private const val EMAIL_SUGGESTIONS_COUNT = 3
    }
}
