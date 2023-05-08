package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

@Stable
sealed interface IsVaultCreatedEvent {
    object Unknown : IsVaultCreatedEvent
    object Created : IsVaultCreatedEvent
}

@Stable
data class BaseVaultUiState(
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val isLoading: IsLoadingState,
    val isTitleRequiredError: Boolean,
    val isCreateButtonEnabled: IsButtonEnabled,
    val isVaultCreatedEvent: IsVaultCreatedEvent
) {
    companion object {
        val Initial = BaseVaultUiState(
            name = "",
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1,
            isLoading = IsLoadingState.NotLoading,
            isTitleRequiredError = false,
            isCreateButtonEnabled = IsButtonEnabled.Disabled,
            isVaultCreatedEvent = IsVaultCreatedEvent.Unknown
        )
    }
}

@Stable
data class CreateVaultUiState(
    val base: BaseVaultUiState,
    val displayNeedUpgrade: Boolean
) {
    companion object {
        val Initial = CreateVaultUiState(
            base = BaseVaultUiState.Initial,
            displayNeedUpgrade = false
        )
    }
}
