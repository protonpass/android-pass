package proton.android.pass.featureitemdetail.impl.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.TotpCopiedToClipbopard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.WebsiteCopiedToClipbopard
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.pass.domain.Item
import proton.pass.domain.ItemType
import javax.inject.Inject

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val observeTotpFromUri: ObserveTotpFromUri
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)
    private val passwordState: MutableStateFlow<PasswordState> =
        MutableStateFlow(getInitialPasswordState())

    private val observeTotpOptionFlow = itemFlow
        .filterNotNull()
        .flatMapLatest { item ->
            val itemContents = item.itemType as ItemType.Login
            val decrypted = encryptionContextProvider.withEncryptionContext {
                decrypt(itemContents.primaryTotp)
            }
            observeTotpFromUri(decrypted)
                .map { flow -> flow.map { it.toOption() } }
                .getOrDefault(flowOf(None))
        }
        .distinctUntilChanged()

    val viewState: StateFlow<LoginDetailUiState> = combine(
        itemFlow.filterNotNull(),
        passwordState,
        observeTotpOptionFlow
    ) { item, password, totpOption ->
        val itemContents = item.itemType as ItemType.Login
        encryptionContextProvider.withEncryptionContext {
            LoginDetailUiState(
                title = decrypt(item.title),
                username = itemContents.username,
                password = password,
                websites = itemContents.websites.toImmutableList(),
                packageNames = itemContents.packageNames.toImmutableList(),
                note = decrypt(item.note),
                totpUiState = totpOption.map { TotpUiState(it.code, it.remainingSeconds) }.value()
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
            withContext(Dispatchers.IO) {
                clipboardManager.copyToClipboard(text = text, isSecure = true)
            }
            snackbarMessageRepository.emitSnackbarMessage(PasswordCopiedToClipboard)
        }
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        itemFlow.value?.let { item ->
            val itemType = item.itemType as ItemType.Login
            withContext(Dispatchers.IO) {
                clipboardManager.copyToClipboard(itemType.username, clearAfterSeconds = null)
            }
            snackbarMessageRepository.emitSnackbarMessage(UsernameCopiedToClipboard)
        }
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(website, clearAfterSeconds = null)
        }
        snackbarMessageRepository.emitSnackbarMessage(WebsiteCopiedToClipbopard)
    }

    fun copyTotpCodeToClipboard(code: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(code)
        }
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
            packageNames = persistentListOf(),
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
