package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetMode
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordSnackbarMessage
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.password.api.PasswordGenerator
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mode = getMode()

    private val _state: MutableStateFlow<GeneratePasswordUiState> =
        MutableStateFlow(getInitialState(mode))

    val state: StateFlow<GeneratePasswordUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = getInitialState(mode)
        )

    fun onLengthChange(value: Int) {
        val password = generatePassword(value, state.value.hasSpecialCharacters)
        _state.value = state.value.copy(length = value, password = password)
    }

    fun onHasSpecialCharactersChange(value: Boolean) {
        val password = generatePassword(state.value.length, value)
        _state.value = state.value.copy(hasSpecialCharacters = value, password = password)
    }

    fun regenerate() {
        val password = generatePassword(state.value.length, state.value.hasSpecialCharacters)
        _state.value = state.value.copy(password = password)
    }

    fun onConfirm() = viewModelScope.launch {
        when (mode) {
            GeneratePasswordMode.CancelConfirm -> storeDraft()
            GeneratePasswordMode.CopyAndClose -> copyToClipboard()
        }
    }

    private suspend fun storeDraft() {
        encryptionContextProvider.withEncryptionContext {
            draftRepository.save(DRAFT_PASSWORD_KEY, encrypt(state.value.password))
        }
    }

    private suspend fun copyToClipboard() {
        clipboardManager.copyToClipboard(state.value.password, isSecure = true)
        snackbarDispatcher(GeneratePasswordSnackbarMessage.CopiedToClipboard)
    }

    private fun getMode(): GeneratePasswordMode {
        val mode = savedStateHandle.get<String>(GeneratePasswordBottomsheetMode.key)
            ?: throw IllegalStateException("Missing ${GeneratePasswordBottomsheetMode.key} nav argument")

        return when (GeneratePasswordBottomsheetModeValue.valueOf(mode)) {
            GeneratePasswordBottomsheetModeValue.CancelConfirm -> GeneratePasswordMode.CancelConfirm
            GeneratePasswordBottomsheetModeValue.CopyAndClose -> GeneratePasswordMode.CopyAndClose
        }
    }

    companion object {

        private fun generatePassword(length: Int, hasSpecialCharacters: Boolean): String {
            val option = when {
                hasSpecialCharacters -> PasswordGenerator.Option.LettersNumbersSymbols
                else -> PasswordGenerator.Option.LettersAndNumbers
            }
            return PasswordGenerator.generatePassword(length, option)
        }

        private fun getInitialState(mode: GeneratePasswordMode): GeneratePasswordUiState =
            GeneratePasswordUiState(
                password = generatePassword(
                    PasswordGenerator.DEFAULT_LENGTH,
                    true
                ),
                length = PasswordGenerator.DEFAULT_LENGTH,
                hasSpecialCharacters = true,
                mode = mode
            )
    }
}
