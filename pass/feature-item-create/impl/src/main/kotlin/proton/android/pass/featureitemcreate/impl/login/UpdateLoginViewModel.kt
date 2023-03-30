package proton.android.pass.featureitemcreate.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.InitError
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.ItemUpdateError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val updateItem: UpdateItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val createAlias: CreateAlias,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    totpManager: TotpManager,
    observeCurrentUser: ObserveCurrentUser,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle,
    draftRepository: DraftRepository
) : BaseLoginViewModel(
    accountManager = accountManager,
    snackbarDispatcher = snackbarDispatcher,
    clipboardManager = clipboardManager,
    totpManager = totpManager,
    observeVaults = observeVaults,
    observeCurrentUser = observeCurrentUser,
    savedStateHandle = savedStateHandle,
    draftRepository = draftRepository
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemId: Option<ItemId> = savedStateHandle.get<String>(CommonNavArgId.ItemId.key)
        .toOption()
        .map { ItemId(it) }

    private var _item: Item? = null

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_item != null) return@launch

            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && navShareId is Some && itemId is Some) {
                itemRepository.getById(userId, navShareId.value, itemId.value)
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
                                    packageInfoSet = item.packageInfoSet.map(::PackageInfoUi)
                                        .toImmutableSet(),
                                    primaryTotp = decrypt(itemContents.primaryTotp),
                                    extraTotpSet = emptySet()
                                )
                            }
                        }
                    }
                    .onError {
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

    fun setAliasItem(aliasItem: AliasItem) {
        canUpdateUsernameState.update { false }
        aliasLocalItemState.update { aliasItem.toOption() }
        loginItemState.update {
            it.copy(
                username = aliasItem.aliasToBeCreated ?: it.username
            )
        }
    }

    fun setTotp(uri: String?) {
        val currentValue = loginItemState.value
        loginItemState.update {
            it.copy(
                primaryTotp = uri ?: currentValue.primaryTotp
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
                val aliasItemOption = aliasLocalItemState.value
                if (aliasItemOption is Some) {
                    performCreateAlias(userId, shareId, aliasItemOption.value)
                        .map { performUpdateItem(userId, shareId, currentItem, loginItem) }
                } else {
                    performUpdateItem(userId, shareId, currentItem, loginItem)
                }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarDispatcher(ItemUpdateError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }

    private suspend fun performCreateAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItem: AliasItem
    ): LoadingResult<Item> =
        if (aliasItem.selectedSuffix != null) {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    title = aliasItem.title,
                    note = aliasItem.note,
                    prefix = aliasItem.prefix,
                    suffix = aliasItem.selectedSuffix.toDomain(),
                    mailboxes = aliasItem.mailboxes
                        .filter { it.selected }
                        .map { it.model }
                        .map(AliasMailboxUiModel::toDomain)
                )
            )
        } else {
            val message = "Empty suffix on create alias"
            PassLogger.i(TAG, message)
            snackbarDispatcher(AliasSnackbarMessage.ItemCreationError)
            LoadingResult.Error(Exception(message))
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
                telemetryManager.sendEvent(ItemUpdate(EventItemType.Login))
            }
            .onError {
                PassLogger.e(TAG, it, "Update item error")
                snackbarDispatcher(ItemUpdateError)
            }
    }

    companion object {
        private const val TAG = "UpdateLoginViewModel"
    }
}
