package me.proton.pass.presentation.create.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.proton.pass.presentation.PasswordGenerator

class CreatePasswordViewModel : ViewModel() {

    private val _state: MutableStateFlow<CreatePasswordUiState> = MutableStateFlow(getInitialState())
    val state: StateFlow<CreatePasswordUiState> = _state
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

    companion object {
        val LENGTH_RANGE = 4.toFloat()..64.toFloat()

        private fun generatePassword(length: Int, hasSpecialCharacters: Boolean): String {
            val option = when {
                hasSpecialCharacters -> PasswordGenerator.Option.LettersNumbersSymbols
                else -> PasswordGenerator.Option.LettersAndNumbers
            }
            return PasswordGenerator.generatePassword(length, option)
        }

        private fun getInitialState(): CreatePasswordUiState =
            CreatePasswordUiState(
                password = generatePassword(
                    PasswordGenerator.DEFAULT_LENGTH,
                    true
                ),
                length = PasswordGenerator.DEFAULT_LENGTH,
                hasSpecialCharacters = true
            )
    }
}
