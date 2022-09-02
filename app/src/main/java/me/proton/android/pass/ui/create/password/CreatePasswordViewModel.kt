package me.proton.android.pass.ui.create.password

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

enum class CharacterSet(val value: String) {
    Alphabet("abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ"),
    Digit("0123456789"),
    Special("!#\$%&()*+.:;<=>?@[]^")
}

class CreatePasswordViewModel : ViewModel() {
    companion object {
        const val DEFAULT_LENGTH = 16
        val LENGTH_RANGE = (4.toFloat()..64.toFloat())

        private fun generatePassword(length: Int, hasSpecialCharacters: Boolean): String {
            var allowedCharacters = CharacterSet.Alphabet.value + CharacterSet.Digit.value
            if (hasSpecialCharacters) {
                allowedCharacters += CharacterSet.Special.value
            }

            return (0 until length)
                .map { allowedCharacters.random() }
                .joinToString("")
        }
    }

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
}