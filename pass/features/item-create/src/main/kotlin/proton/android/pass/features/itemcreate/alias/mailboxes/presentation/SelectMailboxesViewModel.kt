/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.mailboxes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.features.itemcreate.alias.draftrepositories.MailboxDraftRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementMailbox
import javax.inject.Inject

@HiltViewModel
class SelectMailboxesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mailboxDraftRepository: MailboxDraftRepository
) : ViewModel() {

    private val eventFlow = MutableStateFlow<SelectMailboxesEvent>(SelectMailboxesEvent.Idle)

    internal val uiState: StateFlow<SelectMailboxesUiState> = combine(
        mailboxDraftRepository.getAllMailboxesFlow(),
        mailboxDraftRepository.getSelectedMailboxFlow(),
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementMailbox),
        eventFlow
    ) { mailboxes, selectedMailboxes, featureDiscoveryPreference, event ->
        SelectMailboxesUiState(
            mailboxes = mailboxes,
            selectedMailboxes = selectedMailboxes,
            shouldDisplayFeatureDiscoveryBanner = featureDiscoveryPreference.value,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectMailboxesUiState.Initial
    )

    fun toggleMailbox(mailbox: AliasMailbox) {
        mailboxDraftRepository.toggleMailboxById(mailbox.id)
    }

    fun dismissFeatureDiscoveryBanner(addMailbox: Boolean = false) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementMailbox,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
            if (addMailbox) {
                eventFlow.update { SelectMailboxesEvent.AddMailbox }
            }
        }
    }

    fun onConsumeEvent(event: SelectMailboxesEvent) {
        eventFlow.compareAndSet(event, SelectMailboxesEvent.Idle)
    }
}

sealed interface SelectMailboxesEvent {
    data object Idle : SelectMailboxesEvent
    data object AddMailbox : SelectMailboxesEvent
}
