package proton.android.pass.featureprofile.impl

import androidx.compose.runtime.Stable
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled

@Stable
data class ProfileUiState(
    val fingerprintSection: FingerprintSectionState,
    val autofillStatus: AutofillSupportedStatus,
    val itemSummaryUiState: ItemSummaryUiState
) {
    companion object {
        val Initial = ProfileUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            itemSummaryUiState = ItemSummaryUiState()
        )
    }
}

sealed interface FingerprintSectionState {
    data class Available(val enabled: IsButtonEnabled) : FingerprintSectionState
    object NoFingerprintRegistered : FingerprintSectionState
    object NotAvailable : FingerprintSectionState
}

data class ItemSummaryUiState(
    val loginCount: Int = 0,
    val notesCount: Int = 0,
    val aliasCount: Int = 0,
    val mfaCount: Int = 0,
    val aliasLeft: Int? = null,
    val mfaLeft: Int? = null
)
