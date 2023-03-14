package proton.android.pass.featureitemcreate.impl.login.bottomsheet.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonui.api.PasswordGenerator
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordUiState
import proton.android.pass.notifications.api.SnackbarMessageRepository
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    private val _state: MutableStateFlow<GeneratePasswordUiState> =
        MutableStateFlow(getInitialState())
    val state: StateFlow<GeneratePasswordUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = getInitialState()
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
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(state.value.password, isSecure = true)
        }
        snackbarMessageRepository.emitSnackbarMessage(GeneratePasswordSnackbarMessage.CopiedToClipboard)
    }

    companion object {

        private fun generatePassword(length: Int, hasSpecialCharacters: Boolean): String {
            val option = when {
                hasSpecialCharacters -> PasswordGenerator.Option.LettersNumbersSymbols
                else -> PasswordGenerator.Option.LettersAndNumbers
            }
            return PasswordGenerator.generatePassword(length, option)
        }

        private fun getInitialState(): GeneratePasswordUiState =
            GeneratePasswordUiState(
                password = generatePassword(
                    PasswordGenerator.DEFAULT_LENGTH,
                    true
                ),
                length = PasswordGenerator.DEFAULT_LENGTH,
                hasSpecialCharacters = true
            )
    }
}
