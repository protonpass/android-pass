package proton.android.pass.presentation.detail.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.Result
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.presentation.detail.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.presentation.detail.DetailSnackbarMessages.TotpCopiedToClipbopard
import proton.android.pass.presentation.detail.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.presentation.detail.DetailSnackbarMessages.WebsiteCopiedToClipbopard
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import proton.pass.domain.Item
import proton.pass.domain.ItemType
import javax.inject.Inject

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val totpManager: TotpManager
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)
    private val passwordState: MutableStateFlow<PasswordState> =
        MutableStateFlow(getInitialPasswordState())

    private val totsCodeGeneratorFlow: Flow<Pair<String, Int>> = itemFlow
        .filterNotNull()
        .map { item ->
            val itemContents = item.itemType as ItemType.Login
            val decrypted = encryptionContextProvider.withEncryptionContext {
                decrypt(itemContents.primaryTotp)
            }
            totpManager.parse(decrypted)
        }
        .distinctUntilChanged()
        .onEach {
            if (it is Result.Error) {
                PassLogger.w(TAG, it.exception, "Could not parse totp")
            }
        }
        .filterIsInstance<Result.Success<TotpSpec>>()
        .map { it.data }
        .flatMapLatest { specs ->
            totpManager.observeCode(specs)
        }

    val viewState: StateFlow<LoginDetailUiState> = combine(
        itemFlow.filterNotNull(),
        passwordState,
        totsCodeGeneratorFlow
    ) { item, password, test ->
        val itemContents = item.itemType as ItemType.Login
        encryptionContextProvider.withEncryptionContext {
            LoginDetailUiState(
                title = decrypt(item.title),
                username = itemContents.username,
                password = password,
                websites = itemContents.websites.toImmutableList(),
                note = decrypt(item.note),
                totpUiState = TotpUiState(test.first, test.second)
            )
        }
    }
        .stateIn(
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
            snackbarMessageRepository.emitSnackbarMessage(PasswordCopiedToClipboard)
        }
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        itemFlow.value?.let { item ->
            val itemType = item.itemType as ItemType.Login
            clipboardManager.copyToClipboard(itemType.username, clearAfterSeconds = null)
            snackbarMessageRepository.emitSnackbarMessage(UsernameCopiedToClipboard)
        }
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(website, clearAfterSeconds = null)
        snackbarMessageRepository.emitSnackbarMessage(WebsiteCopiedToClipbopard)
    }

    fun copyTotpCodeToClipboard(code: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(code)
        snackbarMessageRepository.emitSnackbarMessage(TotpCopiedToClipbopard)
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

    private fun getInitialState(): LoginDetailUiState =
        LoginDetailUiState(
            title = "",
            username = "",
            password = getInitialPasswordState(),
            websites = persistentListOf(),
            note = "",
            totpUiState = null
        )

    private fun getInitialPasswordState(): PasswordState =
        encryptionContextProvider.withEncryptionContext {
            PasswordState.Concealed(encrypt(""))
        }

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
