package proton.android.pass.featurevault.impl.bottomsheet

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
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
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

    private val formFlow: MutableStateFlow<CreateVaultFormValues> =
        MutableStateFlow(CreateVaultFormValues())
    private val hasEditedTitleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isVaultCreated: MutableStateFlow<IsVaultCreatedEvent> =
        MutableStateFlow(IsVaultCreatedEvent.Unknown)

    val state: StateFlow<CreateVaultUiState> = combine(
        formFlow,
        hasEditedTitleFlow,
        isLoadingFlow,
        isVaultCreated
    ) { form, hasEdited, isLoading, vaultCreated ->
        val isTitleRequiredError = hasEdited && form.name.isBlank()

        CreateVaultUiState(
            name = form.name,
            color = form.color,
            icon = form.icon,
            isLoading = isLoading,
            isTitleRequiredError = isTitleRequiredError,
            isVaultCreatedEvent = vaultCreated,
            isCreateButtonEnabled = IsButtonEnabled.from(form.name.isNotBlank())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CreateVaultUiState.Initial
    )

    fun onNameChange(value: String) {
        formFlow.update { it.copy(name = value) }
        hasEditedTitleFlow.update { true }
    }

    fun onIconChange(value: ShareIcon) {
        formFlow.update { it.copy(icon = value) }
    }

    fun onColorChange(value: ShareColor) {
        formFlow.update { it.copy(color = value) }
    }

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
                snackbarMessageRepository.emitSnackbarMessage(VaultSnackbarMessage.CreateVaultError)
                isLoadingFlow.update { IsLoadingState.NotLoading }
            }
            .logError(PassLogger, TAG, "Create Vault Failed")
    }

    data class CreateVaultFormValues(
        val name: String = "",
        val icon: ShareIcon = ShareIcon.Icon1,
        val color: ShareColor = ShareColor.Color1
    )

    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
