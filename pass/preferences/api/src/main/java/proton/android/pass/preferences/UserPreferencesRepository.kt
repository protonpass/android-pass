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

    fun clearPreferences(): Result<Unit>
}
