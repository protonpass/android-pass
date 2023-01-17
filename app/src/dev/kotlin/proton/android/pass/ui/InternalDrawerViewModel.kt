package proton.android.pass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.UpdateActiveShare
import proton.android.pass.log.api.LogSharing
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.ui.InternalDrawerSnackbarMessage.CannotDeleteCurrentVault
import proton.android.pass.ui.InternalDrawerSnackbarMessage.ChangeVaultError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.ChangeVaultSuccess
import proton.android.pass.ui.InternalDrawerSnackbarMessage.CreateVaultError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.CreateVaultSuccess
import proton.android.pass.ui.InternalDrawerSnackbarMessage.DeleteVaultError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.DeleteVaultSuccess
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val logSharing: LogSharing,
    observeActiveShare: ObserveActiveShare,
    observeShares: ObserveAllShares,
    private val createVault: CreateVault,
    private val deleteVault: DeleteVault,
    private val updateActiveShare: UpdateActiveShare,
    private val cryptoContext: CryptoContext
) : ViewModel() {

    private var shareToModify: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)

    data class ShareUIState(
        val list: List<Share> = emptyList(),
        val shareToModify: Option<ShareId> = None,
        val currentShare: Option<ShareId> = None
    )

    val shareUIState: StateFlow<ShareUIState> = combine(
        observeShares(),
        observeActiveShare(),
        shareToModify
    ) { shareResult, activeShareResult, shareToModify ->
        if (shareResult is Result.Success && activeShareResult is Result.Success) {
            ShareUIState(
                shareResult.data,
                shareToModify,
                activeShareResult.data.toOption()
            )
        } else {
            ShareUIState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShareUIState()
        )

    fun changeCurrentShareToPerformActions(shareId: ShareId) {
        shareToModify.tryEmit(shareId.toOption())
    }

    fun changeSelectedVault() = viewModelScope.launch {
        updateActiveShare(shareId = shareUIState.value.shareToModify.value()!!)
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

    fun deleteVault() = viewModelScope.launch {
        deleteVault(shareUIState.value.shareToModify.value()!!)
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

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .onSuccess {
                snackbarMessageRepository
                    .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesCleared)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error clearing preferences")
                snackbarMessageRepository
                    .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesClearError)
            }
    }

    fun shareLogCatOutput(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        logSharing.shareLogs(appConfig.applicationId, context)
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
        private const val MAX_ID = 10_000
    }
}
