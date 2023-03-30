package proton.android.pass.featureitemcreate.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val createItem: CreateItem,
    private val createItemAndAlias: CreateItemAndAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val draftRepository: DraftRepository,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    totpManager: TotpManager,
    observeCurrentUser: ObserveCurrentUser,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle,
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

    fun setInitialContents(initialContents: InitialCreateLoginUiState) {

        val currentValue = loginItemState.value
        val websites = currentValue.websiteAddresses.toMutableList()

        if (initialContents.url != null) {
            // Check if we are in the initial state, and if so, clear the list
            if (websites.size == 1 && websites.first().isEmpty()) {
                websites.clear()
                websites.add(initialContents.url)
            } else if (!websites.contains(initialContents.url)) {
                websites.add(initialContents.url)
            }
        }
        aliasLocalItemState.update { initialContents.aliasItem.toOption() }
        loginItemState.update {
            val username = initialContents.username
                ?: if (initialContents.aliasItem?.aliasToBeCreated != null) {
                    initialContents.aliasItem.aliasToBeCreated
                } else {
                    currentValue.username
                }
            if (initialContents.aliasItem?.aliasToBeCreated?.isNotEmpty() == true) {
                canUpdateUsernameState.update { false }
            }
            val packageInfoSet = if (initialContents.packageInfoUi != null) {
                it.packageInfoSet.toMutableSet()
                    .apply { add(initialContents.packageInfoUi) }
                    .toImmutableSet()
            } else {
                it.packageInfoSet
            }
            it.copy(
                title = initialContents.title ?: currentValue.title,
                username = username,
                password = initialContents.password ?: currentValue.password,
                websiteAddresses = websites,
                packageInfoSet = packageInfoSet,
                primaryTotp = initialContents.primaryTotp ?: currentValue.primaryTotp
            )
        }
    }

    fun createItem() = viewModelScope.launch(coroutineExceptionHandler) {
        val shouldCreate = validateItem()
        if (!shouldCreate) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val vault = loginUiState.value.selectedVault
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull { userId -> userId != null }
        if (userId != null && vault != null) {
            val aliasItemOption = aliasLocalItemState.value
            if (aliasItemOption is Some) {
                performCreateItemAndAlias(userId, vault.vault.shareId, aliasItemOption.value)
            } else {
                performCreateItem(userId, vault.vault.shareId)
            }
        } else {
            snackbarDispatcher(ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun performCreateItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItem: AliasItem
    ) {
        val selectedSuffix = aliasItem.selectedSuffix
        if (selectedSuffix == null) {
            val message = "Empty suffix on create alias"
            PassLogger.w(TAG, message)
            snackbarDispatcher(ItemCreationError)
            return
        }

        runCatching {
            createItemAndAlias(
                userId = userId,
                shareId = shareId,
                itemContents = loginItemState.value.toItemContents(),
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
        }.onSuccess { item ->
            isItemSavedState.update {
                encryptionContextProvider.withEncryptionContext {
                    ItemSavedState.Success(
                        item.id,
                        item.toUiModel(this@withEncryptionContext)
                    )
                }
            }
            telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
            telemetryManager.sendEvent(ItemCreate(EventItemType.Login))
            draftRepository.delete(CreateAliasViewModel.KEY_DRAFT_ALIAS)
        }.onFailure {
            PassLogger.e(TAG, it, "Could not create item")
            snackbarDispatcher(ItemCreationError)
        }
    }

    private suspend fun performCreateItem(
        userId: UserId,
        shareId: ShareId
    ) {
        createItem(
            userId = userId,
            shareId = shareId,
            itemContents = loginItemState.value.toItemContents()
        )
            .onSuccess { item ->
                isItemSavedState.update {
                    encryptionContextProvider.withEncryptionContext {
                        ItemSavedState.Success(
                            item.id,
                            item.toUiModel(this@withEncryptionContext)
                        )
                    }
                }
                telemetryManager.sendEvent(ItemCreate(EventItemType.Login))
            }
            .onError {
                PassLogger.e(TAG, it, "Could not create item")
                snackbarDispatcher(ItemCreationError)
            }
    }

    companion object {
        private const val TAG = "CreateLoginViewModel"
    }
}
