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

package proton.android.pass.featurefeatureflags.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val ffRepository: FeatureFlagsPreferencesRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    val state = combine(
        ffRepository.get<Boolean>(FeatureFlag.AUTOFILL_DEBUG_MODE),
    ) { autofillDebug -> mapOf(FeatureFlag.AUTOFILL_DEBUG_MODE to autofillDebug) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun <T> override(featureFlag: FeatureFlag, value: T) = viewModelScope.launch {
        if (featureFlag == FeatureFlag.AUTOFILL_DEBUG_MODE) {
            if (value is Boolean && value) {
                notificationManager.showDebugAutofillNotification()
            } else {
                notificationManager.hideDebugAutofillNotification()
            }
        }
        ffRepository.set(featureFlag = featureFlag, value = value)
    }
}
