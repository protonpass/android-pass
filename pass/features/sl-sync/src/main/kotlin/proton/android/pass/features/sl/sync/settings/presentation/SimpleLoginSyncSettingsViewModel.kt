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

package proton.android.pass.features.sl.sync.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncSettingsViewModel @Inject constructor(
    observeDefaultVault: ObserveDefaultVault,
    observeVaultById: ObserveVaultById
) : ViewModel() {

    private val selectedShareIdOptionFlow = MutableStateFlow<Option<ShareId>>(None)

    private val selectedVaultOptionFlow = selectedShareIdOptionFlow
        .flatMapLatest { shareIdOption ->
            when (shareIdOption) {
                None -> observeDefaultVault().map { vaultWithItemCountOption ->
                    when (vaultWithItemCountOption) {
                        None -> None
                        is Some -> vaultWithItemCountOption.value.vault.toOption()
                    }
                }

                is Some -> observeVaultById(shareId = shareIdOption.value)
            }
        }

    internal val state: StateFlow<SimpleLoginSyncSettingsState> = selectedVaultOptionFlow
        .map(::SimpleLoginSyncSettingsState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SimpleLoginSyncSettingsState.Initial
        )

    internal fun onSelectShareId(shareId: ShareId) {
        selectedShareIdOptionFlow.update { shareId.some() }
    }

}
