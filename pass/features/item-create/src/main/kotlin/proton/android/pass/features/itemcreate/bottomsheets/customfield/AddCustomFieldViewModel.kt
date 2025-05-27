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

package proton.android.pass.features.itemcreate.bottomsheets.customfield

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionIndexNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AddCustomFieldViewModel @Inject constructor(
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    val isCustomItemEnabled: StateFlow<Boolean> =
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.CUSTOM_TYPE_V1)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sectionIndex: Option<Int> = savedStateHandle
        .get()
        .require<Int>(CustomSectionIndexNavArgId.key)
        .let { it.takeIf { it >= 0 }.toOption() }
}

