package me.proton.pass.presentation.create.login

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
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.ItemRepository
import me.proton.pass.domain.usecases.GetShareById
import me.proton.pass.domain.usecases.ObserveActiveShare
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.InitError
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.ItemUpdateError
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(snackbarMessageRepository, observeActiveShare, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private var _item: Item? = null

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_item != null) return@launch

            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item ->
                        val itemContents = item.itemType as ItemType.Login
                        _item = item

                        loginItemState.update {
                            LoginItem(
                                title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                username = itemContents.username,
                                password = itemContents.password.decrypt(cryptoContext.keyStoreCrypto),
                                websiteAddresses = itemContents.websites.ifEmpty { listOf("") },
                                note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                            )
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
        val loginItem = loginItemState.value
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null) {
            getShare(userId, shareId)
                .onSuccess { share ->
                    requireNotNull(share)
                    val itemContents = loginItem.toItemContents()
                    itemRepository.updateItem(userId, share, _item!!, itemContents)
                        .onSuccess { item ->
                            isItemSavedState.update { ItemSavedState.Success(item.id) }
                        }
                        .onError {
                            val defaultMessage = "Update item error"
                            PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                            snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
                        }
                }
                .onError {
                    val defaultMessage = "Get share error"
                    PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateLoginViewModel"
    }
}
