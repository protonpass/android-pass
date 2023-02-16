package proton.android.pass.featureitemdetail.impl.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.AliasCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.AliasDetails
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    private val aliasRepository: AliasRepository,
    private val accountManager: AccountManager,
    private val clipboardManager: ClipboardManager,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItem
) : ViewModel() {

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    private val modelState: MutableStateFlow<AliasUiModel?> = MutableStateFlow(null)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)

    val uiState: StateFlow<AliasDetailUiState> = combine(
        isLoadingState,
        isItemSentToTrashState,
        modelState
    ) { loading, isItemSentToTrash, model ->
        AliasDetailUiState(
            isLoadingState = loading.value(),
            isItemSentToTrash = isItemSentToTrash.value(),
            model = model
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AliasDetailUiState.Initial
        )

    fun setItem(item: Item) = viewModelScope.launch {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            modelState.update {
                encryptionContextProvider.withEncryptionContext {
                    AliasUiModel(
                        title = decrypt(item.title),
                        alias = (item.itemType as ItemType.Alias).aliasEmail,
                        mailboxes = it?.mailboxes ?: emptyList(),
                        note = decrypt(item.note)
                    )
                }
            }
            aliasRepository.getAliasDetails(userId, item.shareId, item.id)
                .asResultWithoutLoading()
                .collect { onAliasDetails(it, item) }
        } else {
            showError("UserId is null", DetailSnackbarMessages.InitError, null)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private suspend fun onAliasDetails(result: LoadingResult<AliasDetails>, item: Item) {
        when (result) {
            is LoadingResult.Success -> {
                val alias = item.itemType as ItemType.Alias
                modelState.update {
                    encryptionContextProvider.withEncryptionContext {
                        AliasUiModel(
                            title = decrypt(item.title),
                            alias = alias.aliasEmail,
                            mailboxes = result.data.mailboxes,
                            note = decrypt(item.note)
                        )
                    }
                }
            }
            is LoadingResult.Error -> {
                showError(
                    "Error getting alias details",
                    DetailSnackbarMessages.InitError,
                    result.exception
                )
            }
            else -> {
                // no-op
            }
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun showError(
        message: String,
        snackbarMessage: SnackbarMessage,
        throwable: Throwable? = null
    ) {
        PassLogger.e(TAG, throwable ?: Exception(message), message)
        snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
    }

    fun onCopyAlias(alias: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                clipboardManager.copyToClipboard(alias)
            }
            snackbarMessageRepository.emitSnackbarMessage(AliasCopiedToClipboard)
        }
    }

    fun onDelete(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        trashItem(shareId = shareId, itemId = itemId)
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarMessageRepository.emitSnackbarMessage(ItemMovedToTrash)
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "AliasDetailViewModel"
    }
}
