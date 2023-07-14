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

package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetMode
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordSnackbarMessage
import proton.android.pass.featurepassword.impl.extensions.toContent
import proton.android.pass.featurepassword.impl.extensions.toRandomSpec
import proton.android.pass.featurepassword.impl.extensions.toWordSpec
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.password.api.PasswordGenerator
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.PasswordGenerationPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val mode = getMode()

    private val passwordFlow: MutableStateFlow<String> = MutableStateFlow(getInitialPassword())


    private val passwordGenerationPreference = userPreferencesRepository
        .getPasswordGenerationPreference()
        .distinctUntilChanged()
        .onEach { pref ->
            passwordFlow.update { generatePassword(pref) }
        }

    val state: StateFlow<GeneratePasswordUiState> = combine(
        passwordGenerationPreference,
        passwordFlow
    ) { pref, password ->
        GeneratePasswordUiState(
            password = password,
            mode = mode,
            content = pref.toContent()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = run {
                val pref = runBlocking { getCurrentPreference() }
                GeneratePasswordUiState(
                    password = "",
                    mode = mode,
                    content = pref.toContent()
                )
            }
        )

    fun onLengthChange(value: Int) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomPasswordLength = value)
        updateAndRegenerate(updated)
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
        passwordFlow.update { generatePassword(current) }
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
            draftRepository.save(DRAFT_PASSWORD_KEY, encrypt(state.value.password))
        }
    }

    private fun getInitialPassword(): String {
        val pref = runBlocking { getCurrentPreference() }
        return generatePassword(pref)
    }

    private suspend fun copyToClipboard() {
        clipboardManager.copyToClipboard(state.value.password, isSecure = true)
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

    companion object {

        private fun generatePassword(preference: PasswordGenerationPreference) =
            when (preference.mode) {
                PasswordGenerationMode.Random -> PasswordGenerator.generatePassword(
                    spec = preference.toRandomSpec()
                )
                PasswordGenerationMode.Words -> PasswordGenerator.generateWordPassword(
                    spec = preference.toWordSpec()
                )
            }
    }
}
