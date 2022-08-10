package me.proton.android.pass.ui.detail.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext
) : ViewModel() {

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)
    private val passwordState: MutableStateFlow<PasswordState> = MutableStateFlow(
        PasswordState.Concealed("".encrypt(cryptoContext.keyStoreCrypto))
    )

    val copyToClipboardFlow: MutableSharedFlow<String?> = MutableSharedFlow()
    val initialViewState = getInitialState()
    val viewState = combine(
        itemFlow,
        passwordState,
        ::getUiModel
    )

    fun setItem(item: Item) {
        if (itemFlow.value == null) {
            itemFlow.value = item
        }
    }

    fun copyPasswordToClipboard() = viewModelScope.launch {
        val item = itemFlow.value
        if (item != null) {
            val itemType = item.itemType as ItemType.Login
            val text = when (val password = passwordState.value) {
                is PasswordState.Revealed -> password.clearText
                is PasswordState.Concealed ->
                    itemType.password.decrypt(cryptoContext.keyStoreCrypto)
            }
            copyToClipboardFlow.emit(text)
        }
    }

    fun togglePassword() {
        val item = itemFlow.value ?: return
        val itemType = item.itemType as ItemType.Login

        when (passwordState.value) {
            is PasswordState.Concealed -> {
                passwordState.value = PasswordState.Revealed(
                    encrypted = itemType.password,
                    clearText = itemType.password.decrypt(cryptoContext.keyStoreCrypto)
                )
            }
            is PasswordState.Revealed -> {
                passwordState.value = PasswordState.Concealed(itemType.password)
            }
        }
    }

    private fun getUiModel(item: Item?, password: PasswordState): LoginUiModel {
        if (item == null) return getInitialState()

        val itemContents = item.itemType as ItemType.Login
        return LoginUiModel(
            title = item.title.decrypt(cryptoContext.keyStoreCrypto),
            username = itemContents.username,
            password = password,
            websites = itemContents.websites,
            note = item.note.decrypt(cryptoContext.keyStoreCrypto)
        )
    }

    private fun getInitialState(): LoginUiModel =
        LoginUiModel(
            title = "",
            username = "",
            password = PasswordState.Concealed("".encrypt(cryptoContext.keyStoreCrypto)),
            websites = emptyList(),
            note = ""
        )

    data class LoginUiModel(
        val title: String,
        val username: String,
        val password: PasswordState,
        val websites: List<String>,
        val note: String
    )

    sealed class PasswordState(open val encrypted: EncryptedString) {
        data class Concealed(override val encrypted: EncryptedString) : PasswordState(encrypted)
        data class Revealed(
            override val encrypted: EncryptedString,
            val clearText: String
        ) : PasswordState(encrypted)
    }
}
