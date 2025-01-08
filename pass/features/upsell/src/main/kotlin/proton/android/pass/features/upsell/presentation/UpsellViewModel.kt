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

package proton.android.pass.features.upsell.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.upsell.navigation.UpsellNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class UpsellViewModel @Inject constructor(
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val paidFeature: PaidFeature = savedStateHandleProvider.get()
        .require(UpsellNavArgId.key)

    internal val state = featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
        .map { UpsellState(paidFeature, it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UpsellState(paidFeature, false)
        )

}
