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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.UpdateActiveShare
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotDeleteCurrentVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.ChangeVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.ChangeVaultSuccess
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.DeleteVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.DeleteVaultSuccess
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.MigrateVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.MigrateVaultSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareType
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

@HiltViewModel
class VaultListViewModel @Inject constructor(
    observeActiveShare: ObserveActiveShare,
    observeShares: ObserveAllShares,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val deleteVault: DeleteVault,
    private val migrateVault: MigrateVault,
    private val updateActiveShare: UpdateActiveShare,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    val shareUIState: StateFlow<VaultListUIState> = combine(
        observeShares(),
        observeActiveShare()
    ) { shareResult, activeShareResult ->
        if (shareResult is LoadingResult.Success && activeShareResult is LoadingResult.Success) {
            val shareList = decryptShares(shareResult.data)
            VaultListUIState(
                list = shareList.toPersistentList(),
                currentShare = activeShareResult.data
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

    fun onMigrateVault(toDelete: ShareId?, toMigrateTo: ShareId) = viewModelScope.launch {
        PassLogger.i(TAG, "Share to delete: " + toDelete?.id)
        PassLogger.i(TAG, "Share to migrate: " + toMigrateTo.id)
        toDelete ?: return@launch
        migrateVault(toDelete, toMigrateTo)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(MigrateVaultSuccess)
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(MigrateVaultError)
            }
            .logError(PassLogger, TAG, "Migrate Vault Failed")
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

    private fun decryptShares(shares: List<Share>): List<ShareUiModel> =
        encryptionContextProvider.withEncryptionContext {
            shares.map { share -> decryptShare(this@withEncryptionContext, share) }
        }

    @Suppress("NotImplementedDeclaration")
    private fun decryptShare(encryptionContext: EncryptionContext, share: Share): ShareUiModel =
        when (share.shareType) {
            ShareType.Vault -> {
                when (val content = share.content) {
                    is Some -> {
                        val decrypted = encryptionContext.decrypt(content.value)
                        val parsed = VaultV1.Vault.parseFrom(decrypted)
                        ShareUiModel(share.id, parsed.name)
                    }
                    None -> throw ShareContentNotAvailableError()
                }
            }
            ShareType.Item -> {
                throw NotImplementedError("Item shares are not implemented yet")
            }
        }


    companion object {
        private const val TAG = "VaultListViewModel"
    }
}
