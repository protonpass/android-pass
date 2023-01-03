package me.proton.pass.presentation.detail.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.clipboard.api.ClipboardManager
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.detail.DetailSnackbarMessages
import javax.inject.Inject

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)
    private val passwordState: MutableStateFlow<PasswordState> =
        MutableStateFlow(getInitialPasswordState())

    val viewState: StateFlow<LoginUiModel> = combine(
        itemFlow,
        passwordState,
        ::getUiModel
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = getInitialState()
    )

    fun setItem(item: Item) {
        itemFlow.update { item }
    }

    fun copyPasswordToClipboard() = viewModelScope.launch(coroutineExceptionHandler) {
        itemFlow.value?.let { item ->
            val itemType = item.itemType as ItemType.Login
            val text = when (val password = passwordState.value) {
                is PasswordState.Revealed -> password.clearText
                is PasswordState.Concealed -> {
                    encryptionContextProvider.withEncryptionContext {
                        decrypt(itemType.password)
                    }
                }
            }
            clipboardManager.copyToClipboard(text = text, isSecure = true)
            snackbarMessageRepository.emitSnackbarMessage(DetailSnackbarMessages.PasswordCopiedToClipboard)
        }
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        itemFlow.value?.let { item ->
            val itemType = item.itemType as ItemType.Login
            clipboardManager.copyToClipboard(itemType.username, clearAfterSeconds = null)
            snackbarMessageRepository.emitSnackbarMessage(DetailSnackbarMessages.UsernameCopiedToClipboard)
        }
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(website, clearAfterSeconds = null)
        snackbarMessageRepository.emitSnackbarMessage(DetailSnackbarMessages.WebsiteCopiedToClipbopard)
    }

    fun togglePassword() {
        val item = itemFlow.value ?: return
        val itemType = item.itemType as ItemType.Login

        when (passwordState.value) {
            is PasswordState.Concealed -> {
                encryptionContextProvider.withEncryptionContext {
                    passwordState.value = PasswordState.Revealed(
                        encrypted = itemType.password,
                        clearText = decrypt(itemType.password)
                    )
                }
            }
            is PasswordState.Revealed -> {
                passwordState.value = PasswordState.Concealed(itemType.password)
            }
        }
    }

    private fun getUiModel(item: Item?, password: PasswordState): LoginUiModel {
        if (item == null) return getInitialState()

        val itemContents = item.itemType as ItemType.Login
        return encryptionContextProvider.withEncryptionContext {
            LoginUiModel(
                title = decrypt(item.title),
                username = itemContents.username,
                password = password,
                websites = itemContents.websites.toImmutableList(),
                note = decrypt(item.note)
            )
        }
    }

    private fun getInitialState(): LoginUiModel =
        LoginUiModel(
            title = "",
            username = "",
            password = getInitialPasswordState(),
            websites = persistentListOf(),
            note = ""
        )

    private fun getInitialPasswordState(): PasswordState =
        encryptionContextProvider.withEncryptionContext {
            PasswordState.Concealed(encrypt(""))
        }

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
