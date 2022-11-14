package me.proton.pass.presentation.create.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.ItemRepository
import me.proton.pass.domain.usecases.GetShareById
import me.proton.pass.presentation.create.note.NoteSnackbarMessage.ItemCreationError
import me.proton.pass.presentation.extension.toUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getShare: GetShareById,
    private val itemRepository: ItemRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val keyStoreCrypto: KeyStoreCrypto,
    savedStateHandle: SavedStateHandle
) : BaseNoteViewModel(snackbarMessageRepository, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    fun createNote(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val noteItem = noteItemState.value
        val noteItemValidationErrors = noteItem.validate()
        if (noteItemValidationErrors.isNotEmpty()) {
            noteItemValidationErrorsState.update { noteItemValidationErrors }
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null) {
                getShare(userId, shareId)
                    .onSuccess { share ->
                        requireNotNull(share)
                        val itemContents = noteItem.toItemContents()
                        itemRepository.createItem(userId, share, itemContents)
                            .onSuccess { item ->
                                isItemSavedState.update {
                                    ItemSavedState.Success(
                                        item.id,
                                        item.toUiModel(keyStoreCrypto)
                                    )
                                }
                            }
                            .onError {
                                val defaultMessage = "Create item error"
                                PassLogger.i(
                                    TAG,
                                    it ?: Exception(defaultMessage),
                                    defaultMessage
                                )
                                snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
                            }
                    }
                    .onError {
                        val defaultMessage = "Get share error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
                    }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    companion object {
        private const val TAG = "CreateNoteViewModel"
    }
}
