package proton.android.pass.featurecreateitem.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val createItem: CreateItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    createAlias: CreateAlias,
    accountManager: AccountManager,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(
    createAlias,
    accountManager,
    snackbarMessageRepository,
    observeVaults,
    savedStateHandle
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
        aliasItemState.update { initialContents.aliasItem.toOption() }
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
            val packageNames = if (initialContents.packageName != null) {
                it.packageNames.toMutableSet()
                    .apply { add(initialContents.packageName.packageName) }
                    .toSet()
            } else {
                it.packageNames
            }
            it.copy(
                title = initialContents.title ?: currentValue.title,
                username = username,
                password = initialContents.password ?: currentValue.password,
                websiteAddresses = websites,
                packageNames = packageNames
            )
        }
    }

    fun createItem() = viewModelScope.launch(coroutineExceptionHandler) {
        val shouldCreate = validateItem()
        if (!shouldCreate) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val shareId = loginUiState.value.selectedShareId
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull { userId -> userId != null }
        if (userId != null && shareId != null) {
            val aliasItemOption = aliasItemState.value
            if (aliasItemOption is Some) {
                performCreateAlias(userId, shareId.id, aliasItemOption.value)
                    .map { performCreateItem(userId, shareId.id) }
            } else {
                performCreateItem(userId, shareId.id)
            }
        } else {
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
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
            }
            .onError {
                val defaultMessage = "Could not create item"
                PassLogger.e(TAG, it ?: Exception(defaultMessage), defaultMessage)
                snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
            }
    }

    fun onRemoveAlias() {
        loginItemState.update { it.copy(username = "") }
        aliasItemState.update { None }
        canUpdateUsernameState.update { true }
    }

    companion object {
        private const val TAG = "CreateLoginViewModel"
    }
}
