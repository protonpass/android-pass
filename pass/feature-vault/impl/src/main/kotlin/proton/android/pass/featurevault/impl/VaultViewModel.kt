package proton.android.pass.featurevault.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.UpdateActiveShare
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotDeleteCurrentVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.ChangeVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.ChangeVaultSuccess
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultSuccess
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.DeleteVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.DeleteVaultSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VaultViewModel @Inject constructor(
    observeActiveShare: ObserveActiveShare,
    observeShares: ObserveAllShares,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val createVault: CreateVault,
    private val deleteVault: DeleteVault,
    private val updateActiveShare: UpdateActiveShare,
    private val cryptoContext: CryptoContext
) : ViewModel() {

    val shareUIState: StateFlow<VaultListUIState> = combine(
        observeShares(),
        observeActiveShare()
    ) { shareResult, activeShareResult ->
        if (shareResult is Result.Success && activeShareResult is Result.Success) {
            VaultListUIState(
                shareResult.data
                    .map { share ->
                        val decrypted =
                            cryptoContext.keyStoreCrypto.decrypt(share.content!!)
                        val parsed = VaultV1.Vault.parseFrom(decrypted.array)
                        ShareUiModel(share.id, parsed.name)
                    }
                    .toPersistentList(),
                activeShareResult.data
            )

        } else {
            VaultListUIState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultListUIState()
        )

    fun changeSelectedVault(shareId: ShareId) = viewModelScope.launch {
        updateActiveShare(shareId = shareId)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(ChangeVaultSuccess)
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(ChangeVaultError)
            }
            .logError(PassLogger, TAG, "Change Vault Failed")
    }

    fun onCreateVault() = viewModelScope.launch {
        val id = Random.nextInt(MAX_ID)
        val vault = NewVault(
            name = "Personal-$id".encrypt(cryptoContext.keyStoreCrypto),
            description = "Personal vault-$id".encrypt(cryptoContext.keyStoreCrypto)
        )
        createVault(vault = vault)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(CreateVaultSuccess)
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(CreateVaultError)
            }
            .logError(PassLogger, TAG, "Create Vault Failed")
    }

    fun onDeleteVault(shareId: ShareId) = viewModelScope.launch {
        deleteVault(shareId)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(DeleteVaultSuccess)
            }
            .onError {
                when (it) {
                    is CannotDeleteCurrentVaultError ->
                        snackbarMessageRepository.emitSnackbarMessage(CannotDeleteCurrentVault)
                    else -> snackbarMessageRepository.emitSnackbarMessage(DeleteVaultError)
                }
            }
            .logError(PassLogger, TAG, "Delete Vault Failed")
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
        private const val MAX_ID = 10_000
    }
}
