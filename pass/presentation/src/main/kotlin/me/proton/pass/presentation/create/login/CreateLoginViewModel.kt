package me.proton.pass.presentation.create.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.api.usecases.CreateAlias
import me.proton.android.pass.data.api.usecases.CreateItem
import me.proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.map
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.ItemCreationError
import me.proton.pass.presentation.extension.toUiModel
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val createItem: CreateItem,
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

    private var packageName: PackageName? = null

    fun setInitialContents(initialContents: InitialCreateLoginUiState) {
        packageName = initialContents.packageName

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
            it.copy(
                title = initialContents.title ?: currentValue.title,
                username = username,
                password = initialContents.password ?: currentValue.password,
                websiteAddresses = websites
            )
        }
    }

    fun createItem() {
        when (val shareId = loginUiState.value.shareId) {
            None -> viewModelScope.launch {
                snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
            }
            is Some -> createItem(shareId.value)
        }
    }

    fun createItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val shouldCreate = validateItem()
        if (!shouldCreate) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull { userId -> userId != null }
        if (userId != null) {
            val aliasItemOption = aliasItemState.value
            if (aliasItemOption is Some) {
                performCreateAlias(userId, shareId, aliasItemOption.value)
                    .map { performCreateItem(userId, shareId) }
            } else {
                performCreateItem(userId, shareId)
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
            itemContents = loginItemState.value.toItemContents(),
            packageName = packageName
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
                PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
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
