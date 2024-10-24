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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonrust.api.passwords.PasswordCreator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.passwords.ObservePasswordConfig
import proton.android.pass.features.password.GeneratePasswordBottomsheetMode
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.features.password.GeneratePasswordSnackbarMessage
import proton.android.pass.features.password.extensions.toRandomSpec
import proton.android.pass.features.password.extensions.toWordSpec
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.PasswordGenerationPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    observePasswordConfig: ObservePasswordConfig,
    passwordCreator: PasswordCreator,
    passwordStrengthCalculator: PasswordStrengthCalculator,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val passwordGenerator: proton.android.pass.commonrust.api.PasswordGenerator
) : ViewModel() {

    private val mode = getMode()

    private val passwordConfigFlow = observePasswordConfig()

    private val passwordFlow = passwordConfigFlow.mapLatest(passwordCreator::createPassword)

    private val passwordStrengthFlow =
        passwordFlow.mapLatest(passwordStrengthCalculator::calculateStrength)

    internal val stateFlow: StateFlow<GeneratePasswordUiState> = combine(
        passwordFlow,
        passwordStrengthFlow,
        passwordConfigFlow
    ) { password, passwordStrength, passwordConfig ->
        GeneratePasswordUiState(
            password = password,
            passwordStrength = passwordStrength,
            passwordConfig = passwordConfig,
            mode = mode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GeneratePasswordUiState.initial(mode)
    )

    internal fun onLengthChange(value: Int) {
        viewModelScope.launch {
            val updated = getCurrentPreference().copy(randomPasswordLength = value)
            updateAndRegenerate(updated)
        }
    }

    fun onHasSpecialCharactersChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomHasSpecialCharacters = value)
        updateAndRegenerate(updated)
    }

    fun onCapitalLettersChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomHasCapitalLetters = value)
        updateAndRegenerate(updated)
    }

    fun onIncludeNumbersChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomIncludeNumbers = value)
        updateAndRegenerate(updated)
    }

    fun onPasswordModeChange(value: PasswordGenerationMode) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(mode = value)
        updateAndRegenerate(updated)
    }

    fun onWordsCapitalizeChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(wordsCapitalise = value)
        updateAndRegenerate(updated)
    }

    fun onWordsIncludeNumbersChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(wordsIncludeNumbers = value)
        updateAndRegenerate(updated)
    }

    fun onWordsCountChange(value: Int) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(wordsCount = value)
        updateAndRegenerate(updated)
    }

    fun onWordsSeparatorChange(value: WordSeparator) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(wordsSeparator = value)
        updateAndRegenerate(updated)
    }

    fun regenerate() = viewModelScope.launch {
        val current = getCurrentPreference()
//        passwordFlow.update { generatePassword(current) }
    }

    fun onConfirm() = viewModelScope.launch {
        when (mode) {
            GeneratePasswordMode.CancelConfirm -> storeDraft()
            GeneratePasswordMode.CopyAndClose -> copyToClipboard()
        }
    }

    private fun updateAndRegenerate(pref: PasswordGenerationPreference) {
        userPreferencesRepository.setPasswordGenerationPreference(pref)
    }

    private fun storeDraft() {
        encryptionContextProvider.withEncryptionContext {
            draftRepository.save(DRAFT_PASSWORD_KEY, encrypt(stateFlow.value.password))
        }
    }

    private suspend fun copyToClipboard() {
        clipboardManager.copyToClipboard(stateFlow.value.password, isSecure = true)
        snackbarDispatcher(GeneratePasswordSnackbarMessage.CopiedToClipboard)
    }

    private suspend fun getCurrentPreference(): PasswordGenerationPreference =
        userPreferencesRepository.getPasswordGenerationPreference().first()


    private fun getMode(): GeneratePasswordMode {
        val mode = savedStateHandle.get<String>(GeneratePasswordBottomsheetMode.key)
            ?: throw IllegalStateException("Missing ${GeneratePasswordBottomsheetMode.key} nav argument")

        return when (GeneratePasswordBottomsheetModeValue.valueOf(mode)) {
            GeneratePasswordBottomsheetModeValue.CancelConfirm -> GeneratePasswordMode.CancelConfirm
            GeneratePasswordBottomsheetModeValue.CopyAndClose -> GeneratePasswordMode.CopyAndClose
        }
    }

    private fun generatePassword(preference: PasswordGenerationPreference) = when (preference.mode) {
        PasswordGenerationMode.Random -> passwordGenerator.generatePassword(preference.toRandomSpec())

        PasswordGenerationMode.Words -> passwordGenerator.generatePassphrase(preference.toWordSpec())
    }
}
