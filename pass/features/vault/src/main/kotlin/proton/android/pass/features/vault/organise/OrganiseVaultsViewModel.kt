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

package proton.android.pass.features.vault.organise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByVisibility
import proton.android.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class OrganiseVaultsViewModel @Inject constructor(
    observeVaultsGroupedByVisibility: ObserveVaultsGroupedByVisibility
) : ViewModel() {

    private val pendingVisibilityChangesStateFlow: MutableStateFlow<Map<ShareId, Boolean>> =
        MutableStateFlow(emptyMap())

    val state = combine(
        observeVaultsGroupedByVisibility(),
        pendingVisibilityChangesStateFlow
    ) { (hidden, visible), pending ->
        val allVaults = hidden + visible

        val (finalHidden, finalVisible) = allVaults.partition { vault ->
            when (pending[vault.vault.shareId]) {
                true -> false
                false -> true
                null -> vault in hidden
            }
        }

        OrganiseVaultsUIState(
            hiddenVaults = finalHidden,
            visibleVaults = finalVisible
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OrganiseVaultsUIState.Initial
        )

    fun onConfirm() {
        // To implement
    }

    fun onVisibilityChange(shareId: ShareId, selected: Boolean) {
        pendingVisibilityChangesStateFlow.update {
            it + (shareId to selected)
        }
    }

    companion object {
        private const val TAG = "OrganiseVaultsViewModel"
    }
}
