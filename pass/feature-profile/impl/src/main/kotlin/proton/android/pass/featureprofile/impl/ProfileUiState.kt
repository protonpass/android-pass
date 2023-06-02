package proton.android.pass.featureprofile.impl

import androidx.compose.runtime.Stable
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled

@Stable
data class ProfileUiState(
    val fingerprintSection: FingerprintSectionState,
    val autofillStatus: AutofillSupportedStatus,
    val itemSummaryUiState: ItemSummaryUiState,
    val appVersion: String,
    val accountType: PlanInfo
) {
    companion object {
        fun getInitialState(appVersion: String) = ProfileUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            itemSummaryUiState = ItemSummaryUiState(),
            appVersion = appVersion,
            accountType = PlanInfo.Hide
        )
    }
}

@Stable
sealed interface PlanInfo {

    val accountType: AccountType

    @Stable
    object Hide : PlanInfo {
        override val accountType = AccountType.Free
    }

    @Stable
    object Trial : PlanInfo {
        override val accountType = AccountType.Trial
    }

    @Stable
    data class Unlimited(
        val planName: String,
        override val accountType: AccountType = AccountType.Unlimited
    ) : PlanInfo
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
    val aliasLimit: Int? = null,
    val mfaLimit: Int? = null
)
