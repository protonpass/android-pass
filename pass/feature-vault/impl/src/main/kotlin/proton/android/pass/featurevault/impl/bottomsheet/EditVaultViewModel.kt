package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

@HiltViewModel
class EditVaultViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val updateVault: UpdateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle,
    private val getShareById: GetShareById
) : BaseVaultViewModel() {

    private val shareId = getNavShareId()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    fun onStart() = viewModelScope.launch(coroutineExceptionHandler) {
        formFlow.update { CreateVaultFormValues() }

        hackSoItWorks()

        isLoadingFlow.update { IsLoadingState.Loading }
        getShareById.invoke(shareId = shareId)
            .onSuccess { share ->
                requireNotNull(share)
                setInitialValues(share)
            }
            .logError(PassLogger, TAG, "Error loading share")
        isLoadingFlow.update { IsLoadingState.NotLoading }
    }

    fun onEditClick() = viewModelScope.launch {
        if (formFlow.value.name.isBlank()) return@launch

        isLoadingFlow.update { IsLoadingState.Loading }

        val form = formFlow.value
        val body = encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt(form.name),
                description = encrypt(""),
                icon = form.icon,
                color = form.color
            )
        }

        PassLogger.d(TAG, "Sending Edit Vault request")

        runCatching {
            updateVault(vault = body, shareId = shareId)
        }.onSuccess {
            PassLogger.d(TAG, "Vault edited successfully")
            snackbarMessageRepository.emitSnackbarMessage(VaultSnackbarMessage.EditVaultSuccess)
            isLoadingFlow.update { IsLoadingState.NotLoading }
            isVaultCreated.update { IsVaultCreatedEvent.Created }
        }.onFailure {
            PassLogger.e(TAG, it, "Edit Vault Failed")
            snackbarMessageRepository.emitSnackbarMessage(VaultSnackbarMessage.EditVaultError)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }
    }

    private fun setInitialValues(share: Share) {
        val name = when (val content = share.content) {
            None -> ""
            is Some -> {
                encryptionContextProvider.withEncryptionContext {
                    val decrypted = decrypt(content.value)
                    val parsed = VaultV1.Vault.parseFrom(decrypted)
                    parsed.name
                }
            }
        }

        formFlow.update {
            CreateVaultFormValues(
                name = name,
                icon = share.icon,
                color = share.color
            )
        }
    }

    @Suppress("MagicNumber")
    private suspend fun hackSoItWorks() {
        delay(500)
    }

    private fun getNavShareId(): ShareId {
        val arg = savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
            ?: throw IllegalStateException("Missing ShareID nav argument")
        return ShareId(arg)
    }

    companion object {
        private const val TAG = "EditVaultViewModel"
    }
}
