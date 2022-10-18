package me.proton.core.pass.presentation.create.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.proton.core.pass.presentation.PasswordGenerator

class CreatePasswordViewModel : ViewModel() {

    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState())
    val state: StateFlow<ViewState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState()
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

    data class ViewState(
        val password: String = generatePassword(DEFAULT_LENGTH, true),
        val length: Int = DEFAULT_LENGTH,
        val hasSpecialCharacters: Boolean = true
    )

    companion object {
        private const val DEFAULT_LENGTH = PasswordGenerator.DEFAULT_LENGTH
        val LENGTH_RANGE = 4.toFloat()..64.toFloat()

        private fun generatePassword(length: Int, hasSpecialCharacters: Boolean): String {
            val option = when {
                hasSpecialCharacters -> PasswordGenerator.Option.LettersNumbersSymbols
                else -> PasswordGenerator.Option.LettersAndNumbers
            }
            return PasswordGenerator.generatePassword(length, option)
        }
    }
}
