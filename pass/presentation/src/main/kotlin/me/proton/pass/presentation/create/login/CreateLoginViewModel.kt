package me.proton.pass.presentation.create.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.usecases.CreateItem
import me.proton.pass.domain.usecases.ObserveActiveShare
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.ItemCreationError
import me.proton.pass.presentation.extension.toUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createItem: CreateItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val keyStoreCrypto: KeyStoreCrypto,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(snackbarMessageRepository, observeActiveShare, savedStateHandle) {

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

        loginItemState.update {
            it.copy(
                title = initialContents.title ?: currentValue.title,
                username = initialContents.username ?: currentValue.username,
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
            createItem(userId, shareId, loginItemState.value.toItemContents())
                .onSuccess { item ->
                    isItemSavedState.update {
                        ItemSavedState.Success(
                            item.id,
                            item.toUiModel(keyStoreCrypto)
                        )
                    }
                }
                .onError {
                    val defaultMessage = "Could not create item"
                    PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
                }
        } else {
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "CreateLoginViewModel"
    }
}
