package proton.android.pass.featurevault.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val createVault: CreateVault,
    private val cryptoContext: CryptoContext
) : ViewModel() {

    private val vaultSavedState: MutableStateFlow<VaultSavedState> =
        MutableStateFlow(VaultSavedState.Unknown)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val draftVaultState: MutableStateFlow<DraftVault> =
        MutableStateFlow(DraftVault("", ""))

    data class DraftVault(val title: String, val description: String)

    val createVaultUIState: StateFlow<CreateVaultUIState> = combine(
        draftVaultState,
        isLoadingState,
        vaultSavedState
    ) { draftVault, isLoadingState, vaultSaved ->
        CreateVaultUIState(
            draftVault = draftVault,
            isLoadingState = isLoadingState,
            vaultSavedState = vaultSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateVaultUIState.Initial
        )

    fun onTitleChange(value: String) {
        draftVaultState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        draftVaultState.update { it.copy(description = value) }
    }

    fun onCreateVault(draftVault: DraftVault) = viewModelScope.launch {
        if (draftVault.title.isBlank() || draftVault.description.isBlank()) return@launch
        isLoadingState.update { IsLoadingState.Loading }
        val vault = NewVault(
            name = draftVault.title.encrypt(cryptoContext.keyStoreCrypto),
            description = draftVault.description.encrypt(cryptoContext.keyStoreCrypto)
        )
        createVault(vault = vault)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(CreateVaultSuccess)
                isLoadingState.update { IsLoadingState.NotLoading }
                vaultSavedState.update { VaultSavedState.Success }
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(CreateVaultError)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
            .logError(PassLogger, TAG, "Create Vault Failed")
    }

    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
