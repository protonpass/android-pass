package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider
) : BaseVaultViewModel() {

    fun onCreateClick() = viewModelScope.launch {
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

        PassLogger.d(TAG, "Sending Create Vault request")
        createVault(vault = body)
            .onSuccess {
                PassLogger.d(TAG, "Vault created successfully")
                snackbarMessageRepository.emitSnackbarMessage(VaultSnackbarMessage.CreateVaultSuccess)
                isLoadingFlow.update { IsLoadingState.NotLoading }
                isVaultCreated.update { IsVaultCreatedEvent.Created }
            }
            .onError {
                val message = if (it is CannotCreateMoreVaultsError) {
                    VaultSnackbarMessage.CannotCreateMoreVaultsError
                } else {
                    PassLogger.w(TAG, it, "Create vault failed")
                    VaultSnackbarMessage.CreateVaultError
                }
                snackbarMessageRepository.emitSnackbarMessage(message)

                isLoadingFlow.update { IsLoadingState.NotLoading }
            }
    }


    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
