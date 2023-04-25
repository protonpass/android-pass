package proton.android.pass.featurevault.impl.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class DeleteVaultViewModel @Inject constructor(
    private val getVaultById: GetVaultById,
    private val deleteVault: DeleteVault,
    private val savedStateHandle: SavedStateHandle,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId = getNavShareId()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val vaultNameFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val eventFlow: MutableStateFlow<DeleteVaultEvent> =
        MutableStateFlow(DeleteVaultEvent.Unknown)
    private val formFlow: MutableStateFlow<FormState> = MutableStateFlow(FormState.Initial)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    val state: StateFlow<DeleteVaultUiState> = combine(
        vaultNameFlow,
        eventFlow,
        formFlow,
        isLoadingState
    ) { vaultName, event, form, isLoadingState ->
        DeleteVaultUiState(
            event = event,
            vaultText = form.text,
            isButtonEnabled = form.isButtonEnabled,
            isLoadingState = isLoadingState,
            vaultName = vaultName
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteVaultUiState.Initial
    )

    fun onStart() = viewModelScope.launch(coroutineExceptionHandler) {
        getVaultById(shareId = shareId)
            .asLoadingResult()
            .collect { res ->
                when (res) {
                    LoadingResult.Loading -> {
                        isLoadingState.update { IsLoadingState.Loading }
                    }
                    is LoadingResult.Error -> {
                        PassLogger.w(TAG, res.exception, "Error getting vault by id")
                        snackbarDispatcher(VaultSnackbarMessage.CannotRetrieveVaultError)
                        isLoadingState.update { IsLoadingState.NotLoading }
                    }
                    is LoadingResult.Success -> {
                        vaultNameFlow.update { res.data.name }
                        isLoadingState.update { IsLoadingState.NotLoading }
                    }
                }
            }
    }

    fun onTextChange(text: String) {
        formFlow.update {
            FormState(
                text = text,
                isButtonEnabled = IsButtonEnabled.from(text == vaultNameFlow.value)
            )
        }
    }

    fun onDelete() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { deleteVault.invoke(shareId) }
            .onSuccess {
                snackbarDispatcher(VaultSnackbarMessage.DeleteVaultSuccess)
                eventFlow.update { DeleteVaultEvent.Deleted }
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error deleting vault")
                snackbarDispatcher(VaultSnackbarMessage.DeleteVaultError)
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun getNavShareId(): ShareId {
        val arg = savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
            ?: throw IllegalStateException("Missing ShareID nav argument")
        return ShareId(arg)
    }

    private data class FormState(
        val text: String,
        val isButtonEnabled: IsButtonEnabled
    ) {
        companion object {
            val Initial = FormState(
                text = "",
                isButtonEnabled = IsButtonEnabled.Disabled
            )
        }
    }

    companion object {
        private const val TAG = "DeleteVaultViewModel"
    }

}
