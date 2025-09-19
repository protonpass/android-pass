/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.profile

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.features.profile.accountswitcher.AccountListItem
import proton.android.pass.passkeys.api.PasskeySupport
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.BiometricSystemLockPreference

@Stable
internal sealed interface ProfilePasskeySupportSection {
    data object Hide : ProfilePasskeySupportSection

    @JvmInline
    value class Show(val support: PasskeySupport) : ProfilePasskeySupportSection
}

internal sealed interface ProfileEvent {

    data object Unknown : ProfileEvent

    data object OpenFeatureFlags : ProfileEvent

    data object ConfigurePin : ProfileEvent

    data object HomeLogins : ProfileEvent

    data object HomeAliases : ProfileEvent

    data object HomeCreditCards : ProfileEvent

    data object HomeNotes : ProfileEvent

    data object HomeIdentities : ProfileEvent

    data object HomeCustomItems : ProfileEvent

    data object AllMFA : ProfileEvent

}

@Stable
internal data class ProfileUiState(
    val appLockSectionState: AppLockSectionState,
    val autofillStatus: AutofillSupportedStatus,
    val itemSummaryUiState: ItemSummaryUiState,
    val appVersion: String,
    val accountType: PlanInfo,
    val event: ProfileEvent,
    val showUpgradeButton: Boolean,
    val passkeySupport: ProfilePasskeySupportSection,
    val secureLinksCount: Int,
    val accounts: ImmutableList<AccountListItem>,
    val dataStorageState: DataStorageState
) {

    internal companion object {

        internal fun initial(appVersion: String) = ProfileUiState(
            appLockSectionState = AppLockSectionState.Loading,
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            itemSummaryUiState = ItemSummaryUiState.Default,
            appVersion = appVersion,
            accountType = PlanInfo.Hide,
            event = ProfileEvent.Unknown,
            showUpgradeButton = false,
            passkeySupport = ProfilePasskeySupportSection.Hide,
            secureLinksCount = 0,
            accounts = persistentListOf(),
            dataStorageState = DataStorageState.Initial
        )

    }
}

@Stable
internal sealed interface PlanInfo {

    val accountType: AccountType

    @Stable
    data object Hide : PlanInfo {
        override val accountType = AccountType.Free
    }

    @Stable
    data object Trial : PlanInfo {
        override val accountType = AccountType.Trial
    }

    @Stable
    data class Unlimited(
        val planName: String,
        override val accountType: AccountType = AccountType.Unlimited
    ) : PlanInfo
}

internal sealed interface AppLockSectionState {
    data object Loading : AppLockSectionState
}

internal sealed interface BiometricSection : AppLockSectionState {
    val biometricSystemLockPreference: BiometricSystemLockPreference
}

internal sealed interface PinSection : AppLockSectionState

internal sealed interface UserAppLockSectionState {

    @Stable
    data class Biometric(
        val appLockTimePreference: AppLockTimePreference,
        override val biometricSystemLockPreference: BiometricSystemLockPreference
    ) : UserAppLockSectionState, BiometricSection

    @JvmInline
    @Stable
    value class Pin(
        val appLockTimePreference: AppLockTimePreference
    ) : UserAppLockSectionState, PinSection

    data object None : UserAppLockSectionState, AppLockSectionState

}

internal sealed interface EnforcedAppLockSectionState {

    @Stable
    data class Biometric(
        val seconds: Int,
        override val biometricSystemLockPreference: BiometricSystemLockPreference
    ) : EnforcedAppLockSectionState, BiometricSection

    @JvmInline
    @Stable
    value class Pin(val seconds: Int) : EnforcedAppLockSectionState, PinSection

    @JvmInline
    @Stable
    value class Password(val seconds: Int) : EnforcedAppLockSectionState, AppLockSectionState

}

internal data class ItemSummaryUiState(
    val loginCount: Int,
    val notesCount: Int,
    val aliasCount: Int,
    val creditCardsCount: Int,
    val identityCount: Int,
    val customItemCount: Int,
    val mfaCount: Int,
    val aliasLimit: Int?,
    val mfaLimit: Int?
) {
    companion object {
        val Default = ItemSummaryUiState(
            loginCount = 0,
            notesCount = 0,
            aliasCount = 0,
            creditCardsCount = 0,
            identityCount = 0,
            customItemCount = 0,
            mfaCount = 0,
            aliasLimit = null,
            mfaLimit = null
        )
    }
}
