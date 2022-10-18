package me.proton.core.pass.presentation.create.login

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
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.InitError
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.ItemUpdateError
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(observeActiveShare, savedStateHandle) {

    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private var _item: Item? = null

    init {
        viewModelScope.launch {
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
        private const val TAG = "UpdateLoginViewModel"
    }
}
