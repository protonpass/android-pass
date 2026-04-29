/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username.dialog.separator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.data.api.usecases.usernames.ObserveUsernameConfig
import proton.android.pass.data.api.usecases.usernames.UpdateUsernameConfig
import javax.inject.Inject

@HiltViewModel
class UsernameWordSeparatorViewModel @Inject constructor(
    observeUsernameConfig: ObserveUsernameConfig,
    private val updateUsernameConfig: UpdateUsernameConfig
) : ViewModel() {

    private val eventFlow = MutableStateFlow<UsernameWordSeparatorUiEvent>(UsernameWordSeparatorUiEvent.Idle)

    private val configOptionFlow = observeUsernameConfig()
        .mapLatest { config -> config.some() }

    internal val stateFlow: StateFlow<UsernameWordSeparatorUiState> = combine(
        configOptionFlow,
        eventFlow,
        ::UsernameWordSeparatorUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = UsernameWordSeparatorUiState.Initial
    )

    internal fun onUpdateWordSeparator(newSeparator: PasswordWordSeparator) {
        when (val config = stateFlow.value.configOption) {
            None -> return
            is Some -> viewModelScope.launch {
                config.value.copy(wordSeparator = newSeparator).also { newConfig ->
                    updateUsernameConfig(newConfig)
                    eventFlow.update { UsernameWordSeparatorUiEvent.Close }
                }
            }
        }
    }
}
