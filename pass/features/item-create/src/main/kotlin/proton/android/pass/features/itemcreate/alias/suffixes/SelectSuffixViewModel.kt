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

package proton.android.pass.features.itemcreate.alias.suffixes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementCustomDomain
import javax.inject.Inject

@HiltViewModel
class SelectSuffixViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val mailboxesState: MutableStateFlow<List<String>> =
        MutableStateFlow(emptyList())

    internal val uiState: StateFlow<SelectSuffixUiState> = combine(
        mailboxesState,
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementCustomDomain)
    ) { mailboxes, featureDiscoveryPreference ->
        SelectSuffixUiState(
            suffixList = mailboxes,
            shouldDisplayFeatureDiscoveryBanner = featureDiscoveryPreference.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectSuffixUiState.Initial
    )

    fun selectSuffix(suffix: String) {
        // To implement
    }

    fun dismissFeatureDiscoveryBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementCustomDomain,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
        }
    }
}
