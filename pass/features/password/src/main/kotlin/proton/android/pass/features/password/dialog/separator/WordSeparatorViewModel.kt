/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.dialog.separator

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
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.data.api.usecases.passwords.ObservePasswordConfig
import proton.android.pass.data.api.usecases.passwords.UpdatePasswordConfig
import javax.inject.Inject

@HiltViewModel
class WordSeparatorViewModel @Inject constructor(
    observePasswordConfig: ObservePasswordConfig,
    private val updatePasswordConfig: UpdatePasswordConfig
) : ViewModel() {

    private val eventFlow = MutableStateFlow<WordSeparatorUiEvent>(WordSeparatorUiEvent.Idle)

    private val passwordConfigOptionFlow = observePasswordConfig()
        .mapLatest { config ->
            when (config) {
                is PasswordConfig.Memorable -> config.some()
                is PasswordConfig.Random -> None
            }
        }

    internal val stateFlow: StateFlow<WordSeparatorUiState> = combine(
        passwordConfigOptionFlow,
        eventFlow,
        ::WordSeparatorUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = WordSeparatorUiState.Initial
    )

    internal fun onUpdateWordSeparator(newPasswordWordSeparator: PasswordWordSeparator) {
        when (val config = stateFlow.value.configOption) {
            None -> return
            is Some -> viewModelScope.launch {
                config.value.copy(
                    passwordWordsSeparator = newPasswordWordSeparator
                ).also { newConfig ->
                    updatePasswordConfig(newConfig)

                    eventFlow.update { WordSeparatorUiEvent.Close }
                }
            }
        }
    }

}
