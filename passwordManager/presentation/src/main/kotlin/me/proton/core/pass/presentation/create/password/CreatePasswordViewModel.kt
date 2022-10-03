package me.proton.core.pass.presentation.create.password

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.pass.presentation.PasswordGenerator

class CreatePasswordViewModel : ViewModel() {

    val initialState = ViewState()
    val state: MutableStateFlow<ViewState> = MutableStateFlow(initialState)

    fun onLengthChange(value: Int) {
        val password = generatePassword(value, state.value.hasSpecialCharacters)
        state.value = state.value.copy(length = value, password = password)
    }

    fun onHasSpecialCharactersChange(value: Boolean) {
        val password = generatePassword(state.value.length, value)
        state.value = state.value.copy(hasSpecialCharacters = value, password = password)
    }

    fun regenerate() {
        val password = generatePassword(state.value.length, state.value.hasSpecialCharacters)
        state.value = state.value.copy(password = password)
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
