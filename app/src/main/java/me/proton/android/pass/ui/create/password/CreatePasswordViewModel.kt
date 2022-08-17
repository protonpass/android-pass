package me.proton.android.pass.ui.create.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class CharacterSet(val value: String) {
    Alphabet("abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ"),
    Digit("0123456789"),
    Special("!#\$%&()*+.:;<=>?@[]^")
}

class CreatePasswordViewModel : ViewModel() {
    companion object {
        const val defaultLength = 16
        val lengthRange = (4.toFloat()..64.toFloat())
    }

    val password = MutableStateFlow("")
    val length = MutableStateFlow(defaultLength)
    val hasSpecialCharacters = MutableStateFlow(true)

    init {
        regenerate()
        viewModelScope.launch {
            launch {
                hasSpecialCharacters
                    .distinctUntilChanged { old, new -> old == new }
                    .collectLatest {
                        regenerate()
                    }
            }

            launch {
                length
                    .distinctUntilChanged { old, new -> old == new }
                    .collectLatest { regenerate() }
            }
        }
    }

    fun onLengthChange(value: Int) { length.value = value }

    fun onHasSpecialCharactersChange(value: Boolean) { hasSpecialCharacters.value = value }

    fun regenerate() {
        var allowedCharacters = CharacterSet.Alphabet.value + CharacterSet.Digit.value
        if (hasSpecialCharacters.value) {
            allowedCharacters += CharacterSet.Special.value
        }

        password.value = (0 until length.value)
            .map { allowedCharacters.random() }
            .joinToString("")
    }
}