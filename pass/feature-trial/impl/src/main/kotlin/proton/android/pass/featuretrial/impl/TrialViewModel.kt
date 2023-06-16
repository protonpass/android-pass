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

package proton.android.pass.featuretrial.impl

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.PlanType
import javax.inject.Inject

@HiltViewModel
class TrialViewModel @Inject constructor(
    getUserPlan: GetUserPlan
) : ViewModel() {

    val state: StateFlow<TrialUiState> = getUserPlan().map {
        val remainingDays = when (val plan = it.planType) {
            is PlanType.Trial -> plan.remainingDays
            else -> 0
        }
        TrialUiState(
            remainingTrialDays = remainingDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TrialUiState(remainingTrialDays = 0)
    )

}

@Stable
data class TrialUiState(
    val remainingTrialDays: Int
)
