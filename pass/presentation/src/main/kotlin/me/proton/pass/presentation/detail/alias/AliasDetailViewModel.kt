package me.proton.pass.presentation.detail.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.pass.presentation.detail.DetailSnackbarMessages
import me.proton.pass.presentation.uievents.IsLoadingState
import javax.inject.Inject

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val aliasRepository: me.proton.android.pass.data.api.repositories.AliasRepository,
    private val accountManager: AccountManager,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val loadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    private val modelState: MutableStateFlow<AliasUiModel?> = MutableStateFlow(null)

    val viewState: StateFlow<AliasDetailUiState> = combine(
        loadingState,
        modelState
    ) { loading, model ->
        AliasDetailUiState(
            isLoadingState = loading,
            model = model
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AliasDetailUiState.Initial
        )

    fun setItem(item: Item) = viewModelScope.launch {
        loadingState.update { IsLoadingState.Loading }

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            aliasRepository.getAliasDetails(userId, item.shareId, item.id)
                .asResultWithoutLoading()
                .collect { onAliasDetails(it, item) }
        } else {
            showError("UserId is null", DetailSnackbarMessages.InitError, null)
            loadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun emitSnackbarMessage(message: SnackbarMessage) = viewModelScope.launch {
        snackbarMessageRepository.emitSnackbarMessage(message)
    }

    private suspend fun onAliasDetails(result: Result<AliasDetails>, item: Item) {
        when (result) {
            is Result.Success -> {
                val alias = item.itemType as ItemType.Alias
                modelState.update {
                    AliasUiModel(
                        title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                        alias = alias.aliasEmail,
                        mailboxes = result.data.mailboxes,
                        note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                    )
                }
            }
            is Result.Error -> {
                showError(
                    "Error getting alias details",
                    DetailSnackbarMessages.InitError,
                    result.exception
                )
            }
            else -> {
                // no-op
            }
        }
        loadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun showError(
        message: String,
        snackbarMessage: SnackbarMessage,
        throwable: Throwable? = null
    ) {
        PassLogger.e(TAG, throwable ?: Exception(message), message)
        snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
    }

    companion object {
        private const val TAG = "AliasDetailViewModel"
    }
}
