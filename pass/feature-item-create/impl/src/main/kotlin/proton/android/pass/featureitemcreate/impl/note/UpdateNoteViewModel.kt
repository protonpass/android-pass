package proton.android.pass.featureitemcreate.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.InitError
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.ItemUpdateError
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.NoteUpdated
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle
) : BaseNoteViewModel(observeVaults, savedStateHandle) {

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
            if (userId != null && navShareId is Some && itemId is Some) {
                runCatching { itemRepository.getById(userId, navShareId.value, itemId.value) }
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
                    .onFailure {
                        PassLogger.i(TAG, it, "Get by id error")
                        snackbarDispatcher(InitError)
                    }
            } else {
                PassLogger.i(TAG, "Empty user/share/item Id")
                snackbarDispatcher(InitError)
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
            val itemContents = noteItem.toItemContents()
            runCatching { getShare(userId, shareId) }
                .onFailure {
                    PassLogger.e(TAG, it, "Get share error")
                    snackbarDispatcher(ItemUpdateError)
                }
                .mapCatching { share ->
                    itemRepository.updateItem(userId, share, _item!!, itemContents)
                }
                .onSuccess { item ->
                    isItemSavedState.update {
                        encryptionContextProvider.withEncryptionContext {
                            ItemSavedState.Success(
                                item.id,
                                item.toUiModel(this@withEncryptionContext)
                            )
                        }
                    }
                    snackbarDispatcher(NoteUpdated)
                    telemetryManager.sendEvent(ItemUpdate(EventItemType.Note))
                }
                .onFailure {
                    PassLogger.e(TAG, it, "Update item error")
                    snackbarDispatcher(ItemUpdateError)
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateNoteViewModel"
    }
}
