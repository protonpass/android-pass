package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface AutofillItemOptionsEvent {
    object Unknown: AutofillItemOptionsEvent
    object Close: AutofillItemOptionsEvent
}

@Stable
data class AutofillItemOptionsUiState(
    val isLoading: IsLoadingState,
    val event: AutofillItemOptionsEvent
) {
    companion object {
        val Initial = AutofillItemOptionsUiState(
            isLoading = IsLoadingState.NotLoading,
            event = AutofillItemOptionsEvent.Unknown
        )
    }
}
