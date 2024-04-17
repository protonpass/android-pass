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
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveAddressesByUserId
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForProtonEmail
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.features.security.center.breachdetail.ui.DateUtils
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
internal class DarkWebViewModel @Inject constructor(
    observeItems: ObserveItems,
    observeAddressesByUserId: ObserveAddressesByUserId,
    observeBreachesForProtonEmail: ObserveBreachesForProtonEmail,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeBreachCustomEmails: ObserveBreachCustomEmails,
    observeCustomEmailSuggestions: ObserveCustomEmailSuggestions
) : ViewModel() {

    private val protonEmailFlow = observeAddressesByUserId()
        .flatMapLatest { addresses ->
            if (addresses.isEmpty()) {
                flowOf(emptyMap())
            } else {
                addresses.map { address ->
                    observeBreachesForProtonEmail(addressId = address.addressId)
                }.merge().map { list -> list.groupBy { it.email } }
            }
        }
        .asLoadingResult()

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
                }.merge().map { it.groupBy { it.email } }
            }
        }
        .asLoadingResult()

    private val customEmailsFlow: Flow<LoadingResult<List<BreachCustomEmail>>> =
        observeBreachCustomEmails()
            .asLoadingResult()
            .distinctUntilChanged()

    private val customEmailSuggestionsFlow: Flow<LoadingResult<List<CustomEmailSuggestion>>> =
        observeCustomEmailSuggestions()
            .asLoadingResult()
            .distinctUntilChanged()

    private val darkWebStatusFlow: Flow<LoadingResult<DarkWebStatus>> =
        flowOf(DarkWebStatus.AllGood)
            .asLoadingResult()

    val state: StateFlow<DarkWebUiState> = combine(
        protonEmailFlow,
        aliasEmailFlow,
        customEmailsFlow,
        darkWebStatusFlow,
        customEmailSuggestionsFlow
    ) { protonEmailResult, aliasEmailsResult, customEmailsResult, darkWebStatus, suggestionsResult ->
        DarkWebUiState(
            protonEmailState = getProtonEmailState(protonEmailResult),
            aliasEmailState = getAliasEmailState(aliasEmailsResult),
            customEmailState = getCustomEmailsState(customEmailsResult, suggestionsResult),
            darkWebStatus = darkWebStatus.getOrNull() ?: DarkWebStatus.Loading,
            lastCheckTime = None
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DarkWebUiState.Initial
    )

    private fun getProtonEmailState(
        protonEmailResult: LoadingResult<Map<String, List<BreachEmail>>>
    ): DarkWebEmailBreachState = when (protonEmailResult) {
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Failed to load proton emails")
            PassLogger.w(TAG, protonEmailResult.exception)
            DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad)
        }

        LoadingResult.Loading -> DarkWebEmailBreachState.Loading
        is LoadingResult.Success -> DarkWebEmailBreachState.Success(
            protonEmailResult.data.map {
                EmailBreachUiState(
                    email = it.key,
                    count = it.value.size,
                    breachDate = it.value.mapNotNull {
                        runCatching {
                            Instant.parse(it.publishedAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        }.getOrNull()
                    }.maxOrNull()?.let(DateUtils::formatDate)
                )
            }.toImmutableList()
        )
    }

    private fun getAliasEmailState(
        aliasEmailsResult: LoadingResult<Map<String, List<BreachEmail>>>
    ): DarkWebEmailBreachState = when (aliasEmailsResult) {
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Failed to load alias emails")
            PassLogger.w(TAG, aliasEmailsResult.exception)
            DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad)
        }

        LoadingResult.Loading -> DarkWebEmailBreachState.Loading
        is LoadingResult.Success -> DarkWebEmailBreachState.Success(
            aliasEmailsResult.data.map {
                EmailBreachUiState(
                    email = it.key,
                    count = it.value.size,
                    breachDate = it.value.mapNotNull {
                        runCatching {
                            Instant.parse(it.publishedAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        }.getOrNull()
                    }.maxOrNull()?.let(DateUtils::formatDate)
                )
            }.toImmutableList()
        )
    }

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
                return DarkWebCustomEmailsState.Success(emails.toImmutableList())
            }

            LoadingResult.Loading -> return DarkWebCustomEmailsState.Loading
            is LoadingResult.Success ->
                suggestionsResult.data
                    .filter { it.usedInLoginsCount >= EMAIL_SUGGESTIONS_MIN_USED_IN_COUNT }
                    .map { it.toUiModel() }
        }.take(EMAIL_SUGGESTIONS_COUNT)

        val combined = (verified + unverified + suggestions).toImmutableList()
        return DarkWebCustomEmailsState.Success(combined)
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
