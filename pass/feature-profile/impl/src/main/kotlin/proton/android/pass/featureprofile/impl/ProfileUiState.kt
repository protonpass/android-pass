/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featureprofile.impl

import androidx.compose.runtime.Stable
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled

sealed interface ProfileEvent {
    object Unknown : ProfileEvent
    object OpenFeatureFlags : ProfileEvent
}

@Stable
data class ProfileUiState(
    val fingerprintSection: FingerprintSectionState,
    val autofillStatus: AutofillSupportedStatus,
    val itemSummaryUiState: ItemSummaryUiState,
    val appVersion: String,
    val accountType: PlanInfo,
    val event: ProfileEvent,
    val showUpgradeButton: Boolean
) {
    companion object {
        fun getInitialState(appVersion: String) = ProfileUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            itemSummaryUiState = ItemSummaryUiState(),
            appVersion = appVersion,
            accountType = PlanInfo.Hide,
            event = ProfileEvent.Unknown,
            showUpgradeButton = false
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
    val creditCardsCount: Int = 0,
    val mfaCount: Int = 0,
    val aliasLimit: Int? = null,
    val mfaLimit: Int? = null
)
