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

package proton.android.pass.features.itemcreate.alias.suffixes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.domain.AliasSuffix
import proton.android.pass.features.itemcreate.alias.draftrepositories.SuffixDraftRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementCustomDomain
import javax.inject.Inject

@HiltViewModel
class SelectSuffixViewModel @Inject constructor(
    private val suffixDraftRepository: SuffixDraftRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val eventFlow = MutableStateFlow<SelectSuffixEvent>(SelectSuffixEvent.Idle)

    internal val uiState: StateFlow<SelectSuffixUiState> = combine(
        suffixDraftRepository.getAllSuffixesFlow(),
        suffixDraftRepository.getSelectedSuffixFlow(),
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementCustomDomain),
        eventFlow
    ) { suffixes, selectedSuffix, featureDiscoveryPreference, event ->
        SelectSuffixUiState(
            suffixList = suffixes.toPersistentSet(),
            selectedSuffix = selectedSuffix,
            shouldDisplayFeatureDiscoveryBanner = featureDiscoveryPreference.value,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectSuffixUiState.Initial
    )

    fun selectSuffix(aliasSuffix: AliasSuffix) {
        suffixDraftRepository.selectSuffixById(aliasSuffix.suffix)
        eventFlow.update { SelectSuffixEvent.DismissBottomsheet }
    }

    fun dismissFeatureDiscoveryBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementCustomDomain,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
        }
    }

    fun onConsumeEvent(event: SelectSuffixEvent) {
        eventFlow.compareAndSet(event, SelectSuffixEvent.Idle)
    }
}

sealed interface SelectSuffixEvent {
    data object Idle : SelectSuffixEvent
    data object DismissBottomsheet : SelectSuffixEvent
}
