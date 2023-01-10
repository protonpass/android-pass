package proton.android.pass.featurecreateitem.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.note.NoteSnackbarMessage.InitError
import proton.android.pass.featurecreateitem.impl.note.NoteSnackbarMessage.ItemUpdateError
import proton.android.pass.featurecreateitem.impl.note.NoteSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurecreateitem.impl.note.NoteSnackbarMessage.NoteMovedToTrashError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val trashItem: TrashItem,
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandle: SavedStateHandle
) : BaseNoteViewModel(snackbarMessageRepository, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private var _item: Item? = null

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_item != null) return@launch
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item: Item ->
                        _item = item
                        noteItemState.update {
                            encryptionContextProvider.withEncryptionContext {
                                NoteItem(
                                    title = decrypt(item.title),
                                    note = decrypt(item.note)
                                )
                            }
                        }
                    }
                    .onError {
                        val defaultMessage = "Get by id error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        snackbarMessageRepository.emitSnackbarMessage(InitError)
                    }
            } else {
                PassLogger.i(TAG, "Empty user/share/item Id")
                snackbarMessageRepository.emitSnackbarMessage(InitError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        requireNotNull(_item)
        isLoadingState.update { IsLoadingState.Loading }
        val noteItem = noteItemState.value
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null) {
            getShare(userId, shareId)
                .onSuccess { share ->
                    requireNotNull(share)
                    val itemContents = noteItem.toItemContents()
                    itemRepository.updateItem(userId, share, _item!!, itemContents)
                        .onSuccess { item ->
                            isItemSavedState.update {
                                encryptionContextProvider.withEncryptionContext {
                                    ItemSavedState.Success(
                                        item.id,
                                        item.toUiModel(this@withEncryptionContext)
                                    )
                                }
                            }
                        }
                        .onError {
                            val defaultMessage = "Update item error"
                            PassLogger.e(TAG, it ?: Exception(defaultMessage), defaultMessage)
                            snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
                        }
                }
                .onError {
                    val defaultMessage = "Get share error"
                    PassLogger.e(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onDelete() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }

        val item = _item
        if (userId != null && item != null) {
            trashItem(userId, item.shareId, item.id)
                .onSuccess { snackbarMessageRepository.emitSnackbarMessage(NoteMovedToTrash) }
            isSentToTrashState.update { IsSentToTrashState.Sent }
        } else {
            PassLogger.i(TAG, "Empty userId")
            snackbarMessageRepository.emitSnackbarMessage(NoteMovedToTrashError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateNoteViewModel"
    }
}
