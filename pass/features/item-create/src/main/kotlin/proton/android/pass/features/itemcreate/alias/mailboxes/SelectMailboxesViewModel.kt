/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemcreate.alias.mailboxes

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
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementMailbox
import javax.inject.Inject

@HiltViewModel
class SelectMailboxesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val canUpgradeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val mailboxesState: MutableStateFlow<List<SelectedAliasMailboxUiModel>> =
        MutableStateFlow(emptyList())

    internal val uiState: StateFlow<SelectMailboxesUiState> = combine(
        mailboxesState,
        canUpgradeState,
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementMailbox)
    ) { mailboxes, canUpgrade, featureDiscoveryPreference ->
        val canApply = mailboxes.any { it.selected }
        SelectMailboxesUiState(
            mailboxes = mailboxes,
            canApply = IsButtonEnabled.from(canApply),
            canUpgrade = canUpgrade,
            shouldDisplayFeatureDiscoveryBanner = featureDiscoveryPreference.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectMailboxesUiState.Initial
    )

    internal fun setMailboxes(mailboxes: List<SelectedAliasMailboxUiModel>) {
        mailboxesState.update { mailboxes }
    }

    internal fun onMailboxChanged(newMailbox: SelectedAliasMailboxUiModel) = mailboxesState.value
        .map { mailbox ->
            if (mailbox.model.id == newMailbox.model.id) {
                mailbox.copy(selected = !newMailbox.selected)
            } else {
                mailbox
            }
        }
        .let { mailboxes ->
            mailboxesState.update { mailboxes }
        }

    fun dismissFeatureDiscoveryBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementMailbox,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
        }
    }

}
