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
        private const val minLength = 4
        private const val maxLength = 64
        const val defaultLength = 16
        val lengthRange = (minLength.toFloat()..maxLength.toFloat())
    }

    val password = MutableStateFlow("")
    val length = MutableStateFlow(defaultLength)
    val hasSpecialCharacters = MutableStateFlow(true)

    init {
        regenerate()
    }

    fun onLengthChange(value: Int) {
        length.value = value
        regenerate()
    }

    fun onHasSpecialCharactersChange(value: Boolean) {
        hasSpecialCharacters.value = value
        regenerate()
    }

    fun regenerate() {
        var allowedCharacters = CharacterSet.Alphabet.value + CharacterSet.Digit.value
        if (hasSpecialCharacters.value) {
            allowedCharacters += CharacterSet.Special.value
        }

        password.value = (0..length.value)
            .map { allowedCharacters.random() }
            .joinToString("")
    }
}