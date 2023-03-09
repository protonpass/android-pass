package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

abstract class BaseVaultViewModel : ViewModel() {

    protected val formFlow: MutableStateFlow<CreateVaultFormValues> =
        MutableStateFlow(CreateVaultFormValues())
    protected val hasEditedTitleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isVaultCreated: MutableStateFlow<IsVaultCreatedEvent> =
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

    data class CreateVaultFormValues(
        val name: String = "",
        val icon: ShareIcon = ShareIcon.Icon1,
        val color: ShareColor = ShareColor.Color1
    )
}
