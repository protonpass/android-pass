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
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachProtonEmails
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
import proton.android.pass.features.security.center.PassMonitorDisplayDarkWebMonitoring
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
internal class DarkWebViewModel @Inject constructor(
    observeItems: ObserveItems,
    observeItemCount: ObserveItemCount,
    observeBreachProtonEmails: ObserveBreachProtonEmails,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeBreachCustomEmails: ObserveBreachCustomEmails,
    observeCustomEmailSuggestions: ObserveCustomEmailSuggestions,
    observeGlobalMonitorState: ObserveGlobalMonitorState,
    telemetryManager: TelemetryManager
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayDarkWebMonitoring)
    }

    private val protonEmailFlow = observeBreachProtonEmails()
        .map { protonEmails ->
            protonEmails.filter { protonEmail -> protonEmail.hasBreaches }
        }
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

    internal val state: StateFlow<DarkWebUiState> = combine(
        protonEmailFlowIfEnabled,
        aliasEmailFlowIfEnabled,
        customEmailsFlow,
        customEmailSuggestionsFlow,
        observeItemCount().asLoadingResult()
    ) { protonEmailResult, aliasEmailsResult, customEmailsResult, suggestionsResult, itemCount ->
        val aliasCount = itemCount.getOrNull()?.alias ?: 0
        val canNavigateToAlias = aliasCount > 0
        val protonEmail = getProtonEmailState(protonEmailResult)
        val aliasEmail = getAliasEmailState(aliasEmailsResult)
        val customEmails = getCustomEmailsState(
            protonEmailState = protonEmail,
            aliasEmailState = aliasEmail,
            customEmailsResult = customEmailsResult,
            suggestionsResult = suggestionsResult
        )
        val darkWebStatus = getDarkWebStatus(protonEmail, aliasEmail, customEmails.state)

        DarkWebUiState(
            protonEmailState = protonEmail,
            aliasEmailState = aliasEmail,
            customEmailState = customEmails.state,
            darkWebStatus = darkWebStatus,
            lastCheckTime = None,
            canAddCustomEmails = customEmails.canAddCustomEmails,
            canNavigateToAlias = canNavigateToAlias
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
                    isMonitored = true
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

    @Suppress("LongMethod", "ReturnCount")
    private fun getCustomEmailsState(
        protonEmailState: DarkWebEmailBreachState,
        aliasEmailState: DarkWebEmailBreachState,
        customEmailsResult: LoadingResult<List<BreachCustomEmail>>,
        suggestionsResult: LoadingResult<List<CustomEmailSuggestion>>
    ): CustomEmailsStatus {

        val alreadyAddedProtonEmails = when (protonEmailState) {
            is DarkWebEmailBreachState.Success -> protonEmailState.emails.map { it.email }
            is DarkWebEmailBreachState.Error -> emptyList()
            DarkWebEmailBreachState.Loading -> return CustomEmailsStatus(
                state = DarkWebCustomEmailsState.Loading,
                canAddCustomEmails = false
            )
        }

        val alreadyAddedAliases = when (aliasEmailState) {
            is DarkWebEmailBreachState.Success -> aliasEmailState.emails.map { it.email }
            is DarkWebEmailBreachState.Error -> emptyList()
            DarkWebEmailBreachState.Loading -> return CustomEmailsStatus(
                state = DarkWebCustomEmailsState.Loading,
                canAddCustomEmails = false
            )
        }

        val customEmails = when (customEmailsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom emails")
                PassLogger.w(TAG, customEmailsResult.exception)
                return CustomEmailsStatus(
                    state = DarkWebCustomEmailsState.Error(DarkWebEmailsError.CannotLoad),
                    canAddCustomEmails = false
                )
            }

            LoadingResult.Loading -> return CustomEmailsStatus(
                state = DarkWebCustomEmailsState.Loading,
                canAddCustomEmails = false
            )

            is LoadingResult.Success -> customEmailsResult.data.map { it.toUiModel() }
        }

        val (verified, unverified) = customEmails.partition { it.status is CustomEmailUiStatus.Verified }

        // If we have reached the limit, no suggestions should be shown
        if (customEmails.size >= CUSTOM_EMAILS_LIMIT) {
            return CustomEmailsStatus(
                state = DarkWebCustomEmailsState.Success(
                    emails = (verified + unverified).toImmutableList(),
                    suggestions = persistentListOf()
                ),
                canAddCustomEmails = false
            )
        }

        // We have not reached the limits, calculate the suggestions
        val alreadyAddedCustomEmails = customEmails.map { it.email }
        val alreadyAddedEmails = (alreadyAddedProtonEmails + alreadyAddedAliases + alreadyAddedCustomEmails).toSet()

        val suggestions = when (suggestionsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom email suggestions")
                PassLogger.w(TAG, suggestionsResult.exception)

                // Return the retrieved emails even if suggestions failed
                return CustomEmailsStatus(
                    state = DarkWebCustomEmailsState.Success(
                        emails = customEmails.toImmutableList(),
                        suggestions = persistentListOf()
                    ),
                    canAddCustomEmails = false
                )
            }

            LoadingResult.Loading -> return CustomEmailsStatus(
                state = DarkWebCustomEmailsState.Loading,
                canAddCustomEmails = false
            )

            is LoadingResult.Success ->
                suggestionsResult.data
                    .distinctBy { it.email }
                    .sortedByDescending { it.usedInLoginsCount }
                    .filter { !alreadyAddedEmails.contains(it.email) }
                    .map { it.toUiModel() }
        }.take(EMAIL_SUGGESTIONS_COUNT)

        val combined = (verified + unverified).toImmutableList()
        return CustomEmailsStatus(
            state = DarkWebCustomEmailsState.Success(
                emails = combined,
                suggestions = suggestions.toImmutableList()
            ),
            canAddCustomEmails = true
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

    private data class CustomEmailsStatus(
        val state: DarkWebCustomEmailsState,
        val canAddCustomEmails: Boolean
    )

    companion object {
        private const val TAG = "DarkWebViewModel"

        private const val EMAIL_SUGGESTIONS_COUNT = 3
        private const val CUSTOM_EMAILS_LIMIT = 10
    }
}
