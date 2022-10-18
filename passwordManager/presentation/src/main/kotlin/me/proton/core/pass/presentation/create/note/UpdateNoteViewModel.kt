package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById
import me.proton.core.pass.presentation.create.note.NoteSnackbarMessage.InitError
import me.proton.core.pass.presentation.create.note.NoteSnackbarMessage.ItemUpdateError
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    savedStateHandle: SavedStateHandle
) : BaseNoteViewModel(savedStateHandle) {

    private var _item: Item? = null

    init {
        viewModelScope.launch {
            if (_item != null) return@launch
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item: Item ->
                        _item = item
                        noteItemState.update {
                            NoteItem(
                                title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                            )
                        }
                    }
                    .onError {
                        val defaultMessage = "Get by id error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        mutableSnackbarMessage.tryEmit(InitError)
                    }
            } else {
                PassLogger.i(TAG, "Empty user/share/item Id")
                mutableSnackbarMessage.tryEmit(InitError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch {
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
                            isItemSavedState.update { ItemSavedState.Success(item.id) }
                        }
                        .onError {
                            val defaultMessage = "Update item error"
                            PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                            mutableSnackbarMessage.tryEmit(ItemUpdateError)
                        }
                }
                .onError {
                    val defaultMessage = "Get share error"
                    PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    mutableSnackbarMessage.tryEmit(ItemUpdateError)
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            mutableSnackbarMessage.tryEmit(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateNoteViewModel"
    }
}
