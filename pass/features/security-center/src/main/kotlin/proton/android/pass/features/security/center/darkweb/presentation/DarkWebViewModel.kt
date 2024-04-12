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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
internal class DarkWebViewModel @Inject constructor(
    observeBreachCustomEmails: ObserveBreachCustomEmails,
    observeCustomEmailSuggestions: ObserveCustomEmailSuggestions
) : ViewModel() {

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
        customEmailsFlow,
        darkWebStatusFlow,
        customEmailSuggestionsFlow
    ) { customEmails, darkWebStatus, suggestions ->
        val customEmailsState = getCustomEmailsState(customEmails, suggestions)
        DarkWebUiState(
            customEmails = customEmailsState,
            darkWebStatus = darkWebStatus.getOrNull() ?: DarkWebStatus.Loading,
            lastCheckTime = None
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DarkWebUiState.Initial
    )

    @Suppress("ReturnCount")
    private fun getCustomEmailsState(
        customEmailsResult: LoadingResult<List<BreachCustomEmail>>,
        suggestionsResult: LoadingResult<List<CustomEmailSuggestion>>
    ): DarkWebEmailsState {
        val emails = when (customEmailsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom emails")
                PassLogger.w(TAG, customEmailsResult.exception)
                return DarkWebEmailsState.Error(DarkWebEmailsError.CannotLoad)
            }

            LoadingResult.Loading -> return DarkWebEmailsState.Loading
            is LoadingResult.Success -> customEmailsResult.data.map { it.toUiModel() }
        }

        val (verified, unverified) = emails.partition { it.status is CustomEmailUiStatus.Verified }

        val suggestions = when (suggestionsResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Failed to load custom email suggestions")
                PassLogger.w(TAG, suggestionsResult.exception)

                // Return the retrieved emails even if suggestions failed
                return DarkWebEmailsState.Success(emails.toImmutableList())
            }

            LoadingResult.Loading -> return DarkWebEmailsState.Loading
            is LoadingResult.Success ->
                suggestionsResult.data
                    .filter { it.usedInLoginsCount >= EMAIL_SUGGESTIONS_MIN_USED_IN_COUNT }
                    .map { it.toUiModel() }
        }.take(EMAIL_SUGGESTIONS_COUNT)

        val combined = (verified + unverified + suggestions).toImmutableList()
        return DarkWebEmailsState.Success(combined)
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
