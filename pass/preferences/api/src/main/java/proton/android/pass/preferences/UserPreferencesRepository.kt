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

package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import proton.android.pass.preferences.sentinel.SentinelStatusPreference
import proton.android.pass.preferences.settings.SettingsDisplayAutofillPinningPreference
import proton.android.pass.preferences.settings.SettingsDisplayUsernameFieldPreference
import proton.android.pass.preferences.simplelogin.SimpleLoginSyncStatusPreference

@Suppress("TooManyFunctions", "ComplexInterface")
interface UserPreferencesRepository {
    fun setAppLockState(state: AppLockState): Result<Unit>
    fun getAppLockState(): Flow<AppLockState>

    fun setHasAuthenticated(state: HasAuthenticated): Result<Unit>
    fun getHasAuthenticated(): Flow<HasAuthenticated>

    fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit>
    fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding>

    fun setThemePreference(theme: ThemePreference): Result<Unit>
    fun getThemePreference(): Flow<ThemePreference>

    fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit>
    fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner>

    fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit>
    fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner>

    fun setHasDismissedNotificationBanner(state: HasDismissedNotificationBanner): Result<Unit>
    fun getHasDismissedNotificationBanner(): Flow<HasDismissedNotificationBanner>

    fun setHasDismissedSLSyncBanner(state: HasDismissedSLSyncBanner): Result<Unit>
    fun getHasDismissedSLSyncBanner(): Flow<HasDismissedSLSyncBanner>

    fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit>
    fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard>

    fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit>
    fun getClearClipboardPreference(): Flow<ClearClipboardPreference>

    fun setUseFaviconsPreference(useFavicons: UseFaviconsPreference): Result<Unit>
    fun getUseFaviconsPreference(): Flow<UseFaviconsPreference>

    fun setAppLockTimePreference(preference: AppLockTimePreference): Result<Unit>
    fun getAppLockTimePreference(): Flow<AppLockTimePreference>

    fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit>
    fun getAppLockTypePreference(): Flow<AppLockTypePreference>

    fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit>
    fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference>

    fun setPasswordGenerationPreference(preference: PasswordGenerationPreference): Result<Unit>
    fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference>

    fun setAllowScreenshotsPreference(preference: AllowScreenshotsPreference): Result<Unit>
    fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference>

    fun setDefaultVault(userId: UserId, shareId: ShareId): Result<Unit>
    fun getDefaultVault(userId: UserId): Flow<Option<String>>

    fun tryClearPreferences(): Result<Unit>
    suspend fun clearPreferences(): Result<Unit>

    fun setSentinelStatusPreference(preference: SentinelStatusPreference): Result<Unit>

    fun observeSentinelStatusPreference(): Flow<SentinelStatusPreference>

    fun setMonitorStatusPreference(preference: MonitorStatusPreference): Result<Unit>

    fun observeMonitorStatusPreference(): Flow<MonitorStatusPreference>

    fun setSimpleLoginSyncStatusPreference(preference: SimpleLoginSyncStatusPreference): Result<Unit>

    fun observeSimpleLoginSyncStatusPreference(): Flow<SimpleLoginSyncStatusPreference>

    fun setAliasTrashDialogStatusPreference(preference: AliasTrashDialogStatusPreference): Result<Unit>

    fun observeAliasTrashDialogStatusPreference(): Flow<AliasTrashDialogStatusPreference>

    fun setDisplayUsernameFieldPreference(preference: SettingsDisplayUsernameFieldPreference): Result<Unit>

    fun observeDisplayUsernameFieldPreference(): Flow<SettingsDisplayUsernameFieldPreference>

    fun setDisplayAutofillPinningPreference(preference: SettingsDisplayAutofillPinningPreference): Result<Unit>

    fun observeDisplayAutofillPinningPreference(): Flow<SettingsDisplayAutofillPinningPreference>

    fun observeDisplayFileAttachmentsOnboarding(): Flow<DisplayFileAttachmentsBanner>
    fun setDisplayFileAttachmentsOnboarding(value: DisplayFileAttachmentsBanner): Result<Unit>

    fun setUseDigitalAssetLinksPreference(preference: UseDigitalAssetLinksPreference): Result<Unit>
    fun observeUseDigitalAssetLinksPreference(): Flow<UseDigitalAssetLinksPreference>
}
