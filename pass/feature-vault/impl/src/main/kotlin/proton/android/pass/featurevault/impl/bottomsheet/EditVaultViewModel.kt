package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

@HiltViewModel
class EditVaultViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val updateVault: UpdateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle,
    private val getVaultById: GetVaultById
) : BaseVaultViewModel() {

    private val shareId = getNavShareId()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    fun onStart() = viewModelScope.launch(coroutineExceptionHandler) {
        formFlow.update { CreateVaultFormValues() }

        isLoadingFlow.update { IsLoadingState.Loading }
        kotlin.runCatching {
            getVaultById(shareId = shareId).first()
        }.onSuccess { vault ->
            setInitialValues(vault)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }.onFailure {
            PassLogger.w(TAG, it, "Error getting vault by id")
            snackbarMessageRepository.emitSnackbarMessage(VaultSnackbarMessage.CannotRetrieveVaultError)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }
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

    private fun setInitialValues(vault: Vault) {
        formFlow.update {
            CreateVaultFormValues(
                name = vault.name,
                icon = vault.icon,
                color = vault.color
            )
        }
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
