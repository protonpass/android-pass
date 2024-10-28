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

package proton.android.pass.features.password.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordGenerator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.passwords.ObservePasswordConfig
import proton.android.pass.data.api.usecases.passwords.UpdatePasswordConfig
import proton.android.pass.features.password.GeneratePasswordBottomsheetMode
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.features.password.GeneratePasswordSnackbarMessage
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    stateHandleProvider: SavedStateHandleProvider,
    observePasswordConfig: ObservePasswordConfig,
    passwordGenerator: PasswordGenerator,
    passwordStrengthCalculator: PasswordStrengthCalculator,
    private val updatePasswordConfig: UpdatePasswordConfig,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val mode = stateHandleProvider.get()
        .require<String>(GeneratePasswordBottomsheetMode.key)
        .let { modeValue ->
            when (GeneratePasswordBottomsheetModeValue.valueOf(modeValue)) {
                GeneratePasswordBottomsheetModeValue.CancelConfirm -> GeneratePasswordMode.CancelConfirm
                GeneratePasswordBottomsheetModeValue.CopyAndClose -> GeneratePasswordMode.CopyAndClose
            }
        }

    private val passwordConfigFlow = observePasswordConfig()

    private val regeneratePasswordFlow = MutableStateFlow(false)

    private val passwordFlow = combine(
        passwordConfigFlow,
        regeneratePasswordFlow
            .onStart { emit(true) }
            .onEach { regeneratePasswordFlow.update { false } }
            .filter { it }
    ) { passwordConfig, _ ->
        passwordGenerator.generatePassword(passwordConfig)
    }

    private val passwordStrengthFlow = passwordFlow
        .mapLatest(passwordStrengthCalculator::calculateStrength)

    private val eventFlow = MutableStateFlow<GeneratePasswordEvent>(GeneratePasswordEvent.Idle)

    internal val stateFlow: StateFlow<GeneratePasswordUiState> = combine(
        passwordFlow,
        passwordStrengthFlow,
        passwordConfigFlow,
        eventFlow
    ) { password, passwordStrength, passwordConfig, event ->
        GeneratePasswordUiState(
            password = password,
            passwordStrength = passwordStrength,
            passwordConfig = passwordConfig,
            mode = mode,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GeneratePasswordUiState.initial(mode)
    )

    internal fun onConsumeEvent(event: GeneratePasswordEvent) {
        eventFlow.compareAndSet(event, GeneratePasswordEvent.Idle)
    }

    internal fun onChangePasswordConfig(newPasswordConfig: PasswordConfig) {
        viewModelScope.launch {
            updatePasswordConfig(newPasswordConfig)
        }
    }

    internal fun onRegeneratePassword() {
        regeneratePasswordFlow.update { true }
    }

    internal fun onConfirmPassword() {
        viewModelScope.launch {
            encryptionContextProvider.withEncryptionContextSuspendable {
                encrypt(stateFlow.value.password)
            }.also { encryptedPassword ->
                draftRepository.save(DRAFT_PASSWORD_KEY, encryptedPassword)
                eventFlow.update { GeneratePasswordEvent.OnPasswordConfirmed }
            }
        }
    }

    internal fun onCopyPassword() {
        clipboardManager.copyToClipboard(stateFlow.value.password, isSecure = true)

        viewModelScope.launch {
            snackbarDispatcher(GeneratePasswordSnackbarMessage.CopiedToClipboard)
            eventFlow.update { GeneratePasswordEvent.OnPasswordCopied }
        }
    }

}
