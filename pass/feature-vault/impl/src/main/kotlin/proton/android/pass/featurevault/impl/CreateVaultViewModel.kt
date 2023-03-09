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
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CreateVaultSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val vaultSavedState: MutableStateFlow<VaultSavedState> =
        MutableStateFlow(VaultSavedState.Unknown)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val validationErrorsState: MutableStateFlow<Set<DraftVaultValidationErrors>> =
        MutableStateFlow(emptySet())
    private val draftVaultState: MutableStateFlow<DraftVaultUiState> =
        MutableStateFlow(DraftVaultUiState("", ""))

    val createVaultUIState: StateFlow<CreateVaultUIState> = combine(
        draftVaultState,
        isLoadingState,
        vaultSavedState,
        validationErrorsState
    ) { draftVault, isLoadingState, vaultSaved, validationErrors ->
        CreateVaultUIState(
            draftVault = draftVault,
            isLoadingState = isLoadingState,
            vaultSavedState = vaultSaved,
            validationErrors = validationErrors
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

    fun onCreateVault(draftVault: DraftVaultUiState) = viewModelScope.launch {
        val validationErrors = draftVault.validate()
        if (validationErrors.isEmpty()) {
            validationErrorsState.value = emptySet()
            isLoadingState.update { IsLoadingState.Loading }

            val vault = encryptionContextProvider.withEncryptionContext {
                NewVault(
                    name = encrypt(draftVault.title),
                    description = encrypt(draftVault.description),
                    icon = ShareIcon.Icon1,
                    color = ShareColor.Color1
                )
            }
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
        } else {
            validationErrorsState.update { validationErrors }
        }
    }

    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
