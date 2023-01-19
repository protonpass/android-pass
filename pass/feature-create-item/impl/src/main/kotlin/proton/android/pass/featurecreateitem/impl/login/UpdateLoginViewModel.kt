package proton.android.pass.featurecreateitem.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.InitError
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.ItemUpdateError
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.LoginMovedToTrash
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.LoginMovedToTrashError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val updateItem: UpdateItem,
    private val trashItem: TrashItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    createAlias: CreateAlias,
    accountManager: AccountManager,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(
    createAlias,
    accountManager,
    snackbarMessageRepository,
    observeActiveShare,
    savedStateHandle
) {

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
                            encryptionContextProvider.withEncryptionContext {
                                val websites = if (itemContents.websites.isEmpty()) {
                                    persistentListOf("")
                                } else {
                                    itemContents.websites.toImmutableList()
                                }
                                LoginItem(
                                    title = decrypt(item.title),
                                    username = itemContents.username,
                                    password = decrypt(itemContents.password),
                                    websiteAddresses = websites,
                                    note = decrypt(item.note),
                                    packageNames = item.allowedPackageNames.toSet()
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

    fun setAliasItem(aliasItem: AliasItem) {
        canUpdateUsernameState.update { false }
        aliasItemState.update { aliasItem.toOption() }
        loginItemState.update {
            it.copy(
                username = aliasItem.aliasToBeCreated ?: it.username
            )
        }
    }

    fun updateItem(shareId: ShareId) =
        viewModelScope.launch(coroutineExceptionHandler) {
            val currentItem = _item
            requireNotNull(currentItem)
            val shouldUpdate = validateItem()
            if (!shouldUpdate) return@launch

            isLoadingState.update { IsLoadingState.Loading }
            val loginItem = loginItemState.value
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null) {
                val aliasItemOption = aliasItemState.value
                if (aliasItemOption is Some) {
                    performCreateAlias(userId, shareId, aliasItemOption.value)
                        .map { performUpdateItem(userId, shareId, currentItem, loginItem) }
                } else {
                    performUpdateItem(userId, shareId, currentItem, loginItem)
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
                .onSuccess { snackbarMessageRepository.emitSnackbarMessage(LoginMovedToTrash) }
            isItemSentToTrashState.update { IsSentToTrashState.Sent }
        } else {
            PassLogger.i(TAG, "Empty userId")
            snackbarMessageRepository.emitSnackbarMessage(LoginMovedToTrashError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun performUpdateItem(
        userId: UserId,
        shareId: ShareId,
        currentItem: Item,
        loginItem: LoginItem
    ) {
        updateItem(userId, shareId, currentItem, loginItem.toItemContents())
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

    companion object {
        private const val TAG = "UpdateLoginViewModel"
    }
}
