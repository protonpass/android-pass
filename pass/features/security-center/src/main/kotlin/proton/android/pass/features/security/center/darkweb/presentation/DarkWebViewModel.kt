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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.map
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.RefreshBreaches
import proton.android.pass.data.api.usecases.breach.AddBreachCustomEmail
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveBreachAliasEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachProtonEmails
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.domain.breach.AliasData
import proton.android.pass.domain.breach.AliasKeyId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.features.security.center.PassMonitorDisplayDarkWebMonitoring
import proton.android.pass.features.security.center.customemail.presentation.SecurityCenterCustomEmailSnackbarMessage
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
internal class DarkWebViewModel @Inject constructor(
    observeBreachProtonEmails: ObserveBreachProtonEmails,
    observeBreachCustomEmails: ObserveBreachCustomEmails,
    observeBreachAliasEmails: ObserveBreachAliasEmails,
    observeCustomEmailSuggestions: ObserveCustomEmailSuggestions,
    observeGlobalMonitorState: ObserveGlobalMonitorState,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    refreshBreaches: RefreshBreaches,
    telemetryManager: TelemetryManager,
    private val addBreachCustomEmail: AddBreachCustomEmail,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayDarkWebMonitoring)
        viewModelScope.launch {
            val areUserEventsEnabled = featureFlagsPreferencesRepository.get<Boolean>(
                FeatureFlag.PASS_USER_EVENTS_V1
            ).firstOrNull()
            areUserEventsEnabled ?: return@launch
            if (!areUserEventsEnabled) {
                safeRunCatching {
                    refreshBreaches()
                }
            }
        }
    }

    private val eventFlow: MutableStateFlow<DarkWebEvent> = MutableStateFlow(DarkWebEvent.Idle)
    private val loadingSuggestionFlow: MutableStateFlow<Option<String>> =
        MutableStateFlow(None)

    private val protonEmailFlowIfEnabled = observeGlobalMonitorState()
        .map { it.protonMonitorEnabled }
        .flatMapLatest { protonMonitorEnabled ->
            if (protonMonitorEnabled) {
                observeBreachProtonEmails().map { list -> list to true }
            } else {
                flowOf(emptyList<BreachProtonEmail>() to false)
            }
        }
        .asLoadingResult()

    private val aliasEmailFlowIfEnabled = observeGlobalMonitorState()
        .map { it.protonMonitorEnabled }
        .flatMapLatest { protonMonitorEnabled ->
            if (protonMonitorEnabled) {
                observeBreachAliasEmails().asLoadingResult()
                    .map { result -> result.map { it to true } }
            } else {
                flowOf(LoadingResult.Success(emptyMap<AliasKeyId, AliasData>() to false))
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

    internal val state: StateFlow<DarkWebUiState> = combineN(
        protonEmailFlowIfEnabled,
        aliasEmailFlowIfEnabled,
        customEmailsFlow,
        customEmailSuggestionsFlow,
        eventFlow,
        loadingSuggestionFlow
    ) { protonEmailResult, aliasEmailsResult, customEmailsResult, suggestionsResult,
        event, loadingSuggestion ->
        val protonEmail = getProtonEmailState(protonEmailResult)
        val aliasEmail = getAliasEmailState(aliasEmailsResult)
        val customEmails = getCustomEmailsState(
            protonEmailState = protonEmail,
            aliasEmailState = aliasEmail,
            customEmailsResult = customEmailsResult,
            suggestionsResult = suggestionsResult,
            loadingSuggestion = loadingSuggestion
        )
        val darkWebStatus = getDarkWebStatus(protonEmail, aliasEmail, customEmails.state)

        DarkWebUiState(
            protonEmailState = protonEmail,
            aliasEmailState = aliasEmail,
            customEmailState = customEmails.state,
            darkWebStatus = darkWebStatus,
            lastCheckTime = None,
            canAddCustomEmails = customEmails.canAddCustomEmails,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DarkWebUiState.Initial
    )

    fun onAddSuggestion(suggestion: String) = viewModelScope.launch {
        loadingSuggestionFlow.update { suggestion.some() }
        runCatching {
            addBreachCustomEmail(email = suggestion)
        }.onSuccess { breachCustomEmail ->
            val event = DarkWebEvent.OnVerifyCustomEmail(
                email = breachCustomEmail.email,
                customEmailId = breachCustomEmail.id
            )
            eventFlow.update { event }
        }.onFailure {
            PassLogger.w(TAG, "Error adding custom email")
            PassLogger.w(TAG, it)
            snackbarDispatcher(SecurityCenterCustomEmailSnackbarMessage.ErrorAddingEmail)
        }
        loadingSuggestionFlow.update { None }
    }

    fun consumeEvent(event: DarkWebEvent) = viewModelScope.launch {
        eventFlow.compareAndSet(event, DarkWebEvent.Idle)
    }

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
            }.sortedByDescending { it.isMonitored }.toImmutableList(),
            protonEmailResult.data.second
        )
    }

    private fun getAliasEmailState(
        aliasEmailsResult: LoadingResult<Pair<Map<AliasKeyId, AliasData>, Boolean>>
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
                    id = BreachEmailId.Alias(
                        BreachId(""),
                        it.key.shareId,
                        it.key.itemId
                    ),
                    email = it.key.alias,
                    count = it.value.breaches.size,
                    breachDate = it.value.breaches.getLatestBreachDate(),
                    isMonitored = it.value.isMonitored
                )
            }.sortedByDescending { it.isMonitored }.toImmutableList(),
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
        suggestionsResult: LoadingResult<List<CustomEmailSuggestion>>,
        loadingSuggestion: Option<String>
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
        val alreadyAddedEmails =
            (alreadyAddedProtonEmails + alreadyAddedAliases + alreadyAddedCustomEmails).toSet()

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
                    .map {
                        it.toUiModel(loadingSuggestion.value() == it.email)
                    }
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

    private fun CustomEmailSuggestion.toUiModel(loading: Boolean) = CustomEmailUiState(
        email = email,
        status = CustomEmailUiStatus.Suggestion(
            usedInLoginsCount = usedInLoginsCount,
            isLoadingState = IsLoadingState.from(loading)
        )
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
