package proton.android.pass.featureitemdetail.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotPermanentlyDeleted
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemPermanentlyDeleted
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.TotpCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.WebsiteCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject
import proton.android.pass.common.api.combine as combineN

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val observeTotpFromUri: ObserveTotpFromUri,
    private val trashItem: TrashItem,
    private val deleteItem: DeleteItem,
    private val restoreItem: RestoreItem,
    private val telemetryManager: TelemetryManager,
    getItemById: GetItemById,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))
    private val itemId: ItemId =
        ItemId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ItemId.key)))

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val passwordState: MutableStateFlow<PasswordState> =
        MutableStateFlow(getInitialPasswordState())
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)

    private val observeTotpOptionFlow = getItemById(shareId, itemId)
        .flatMapLatest { itemLoadingResult ->
            val item = itemLoadingResult.getOrNull()
            item ?: return@flatMapLatest flowOf(None)
            val itemContents = item.itemType as ItemType.Login
            val decrypted = encryptionContextProvider.withEncryptionContext {
                decrypt(itemContents.primaryTotp)
            }
            observeTotpFromUri(decrypted)
                .map { flow -> flow.map { it.toOption() } }
                .getOrDefault(flowOf(None))
        }
        .distinctUntilChanged()

    val uiState: StateFlow<LoginDetailUiState> = combineN(
        getItemById(shareId, itemId),
        passwordState,
        observeTotpOptionFlow,
        isLoadingState,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState
    ) { itemLoadingResult,
        password,
        totpOption,
        isLoading,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(InitError)
                LoginDetailUiState.Error
            }
            LoadingResult.Loading -> LoginDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                encryptionContextProvider.withEncryptionContext {
                    LoginDetailUiState.Success(
                        itemUiModel = itemLoadingResult.data.toUiModel(this),
                        passwordState = password,
                        totpUiState = totpOption
                            .map { TotpUiState(it.code, it.remainingSeconds, it.totalSeconds) }
                            .value(),
                        isLoading = isLoading.value(),
                        isItemSentToTrash = isItemSentToTrash.value(),
                        isPermanentlyDeleted = isPermanentlyDeleted.value(),
                        isRestoredFromTrash = isRestoredFromTrash.value()
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginDetailUiState.NotInitialised
        )

    fun copyPasswordToClipboard() = viewModelScope.launch(coroutineExceptionHandler) {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemUiModel.itemType as? ItemType.Login ?: return@launch
        val text = when (val password = passwordState.value) {
            is PasswordState.Revealed -> password.clearText
            is PasswordState.Concealed -> encryptionContextProvider.withEncryptionContext {
                decrypt(itemType.password)
            }
        }
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(text = text, isSecure = true)
        }
        snackbarDispatcher(PasswordCopiedToClipboard)
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemUiModel.itemType as? ItemType.Login ?: return@launch
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(itemType.username)
        }
        snackbarDispatcher(UsernameCopiedToClipboard)
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(website)
        }
        snackbarDispatcher(WebsiteCopiedToClipboard)
    }

    fun copyTotpCodeToClipboard(code: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(code)
        }
        snackbarDispatcher(TotpCopiedToClipboard)
    }

    fun togglePassword() = viewModelScope.launch(coroutineExceptionHandler) {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemUiModel.itemType as? ItemType.Login ?: return@launch

        when (passwordState.value) {
            is PasswordState.Concealed ->
                encryptionContextProvider.withEncryptionContext {
                    passwordState.value = PasswordState.Revealed(
                        encrypted = itemType.password,
                        clearText = decrypt(itemType.password)
                    )
                }
            is PasswordState.Revealed ->
                passwordState.value = PasswordState.Concealed(itemType.password)
        }
    }

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        trashItem(shareId = shareId, itemId = itemId)
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onError {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onPermanentlyDelete(shareId: ShareId, itemId: ItemId, itemType: ItemType) =
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                deleteItem(shareId = shareId, itemId = itemId)
            }.onSuccess {
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemType)))
                isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
                snackbarDispatcher(ItemPermanentlyDeleted)
                PassLogger.i(TAG, "Item deleted successfully")
            }.onFailure {
                snackbarDispatcher(ItemNotPermanentlyDeleted)
                PassLogger.i(TAG, it, "Could not delete item")
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }

    fun onItemRestore(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            restoreItem(shareId = shareId, itemId = itemId)
        }.onSuccess {
            isRestoredFromTrashState.update { IsRestoredFromTrashState.Restored }
            PassLogger.i(TAG, "Item restored successfully")
            snackbarDispatcher(ItemRestored)
        }.onFailure {
            PassLogger.i(TAG, it, "Error restoring item")
            snackbarDispatcher(ItemNotRestored)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun getInitialPasswordState(): PasswordState =
        encryptionContextProvider.withEncryptionContext {
            PasswordState.Concealed(encrypt(""))
        }

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
