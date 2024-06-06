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

package proton.android.pass.features.extrapassword.confirm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.extrapassword.SetupExtraPassword
import proton.android.pass.features.extrapassword.confirm.navigation.EncryptedPasswordNavArgId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class ConfirmExtraPasswordViewModel @Inject constructor(
    private val setupExtraPassword: SetupExtraPassword,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val encryptedPassword = savedStateHandleProvider.get()
        .require<EncryptedString>(EncryptedPasswordNavArgId.key)

    private val eventFlow: MutableStateFlow<ConfirmExtraPasswordContentEvent> =
        MutableStateFlow(ConfirmExtraPasswordContentEvent.Idle)
    private val isLoadingFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val state: StateFlow<ConfirmExtraPasswordNameUiState> = combine(
        eventFlow,
        isLoadingFlow,
        ::ConfirmExtraPasswordNameUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ConfirmExtraPasswordNameUiState.Initial
    )

    internal fun submit() {
        viewModelScope.launch {
            isLoadingFlow.update { IsLoadingState.Loading }
            runCatching {
                setupExtraPassword(encryptedPassword)
            }.onSuccess {
                PassLogger.i(TAG, "Extra password setup successful")
                eventFlow.update { ConfirmExtraPasswordContentEvent.Success }
            }.onError {
                PassLogger.w(TAG, it)
                PassLogger.w(TAG, "Extra password setup failed")
            }
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }
    }

    internal fun onEventConsumed(event: ConfirmExtraPasswordContentEvent) {
        eventFlow.compareAndSet(event, ConfirmExtraPasswordContentEvent.Idle)
    }

    private companion object {
        private const val TAG = "ConfirmExtraPasswordViewModel"
    }
}


