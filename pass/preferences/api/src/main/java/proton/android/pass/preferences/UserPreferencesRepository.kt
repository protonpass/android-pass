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
    suspend fun setBiometricLockState(state: BiometricLockState): Result<Unit>
    fun getBiometricLockState(): Flow<BiometricLockState>

    suspend fun setHasAuthenticated(state: HasAuthenticated): Result<Unit>
    fun getHasAuthenticated(): Flow<HasAuthenticated>

    suspend fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit>
    fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding>

    suspend fun setThemePreference(theme: ThemePreference): Result<Unit>
    fun getThemePreference(): Flow<ThemePreference>

    suspend fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit>
    fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner>

    suspend fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit>
    fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner>

    suspend fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit>
    fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard>

    suspend fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit>
    fun getClearClipboardPreference(): Flow<ClearClipboardPreference>

    suspend fun setUseFaviconsPreference(useFavicons: UseFaviconsPreference): Result<Unit>
    fun getUseFaviconsPreference(): Flow<UseFaviconsPreference>

    suspend fun setAppLockTimePreference(preference: AppLockTimePreference): Result<Unit>
    fun getAppLockTimePreference(): Flow<AppLockTimePreference>

    suspend fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit>
    fun getAppLockTypePreference(): Flow<AppLockTypePreference>

    suspend fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit>
    fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference>

    suspend fun setPasswordGenerationPreference(preference: PasswordGenerationPreference): Result<Unit>
    fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference>

    suspend fun setAllowScreenshotsPreference(preference: AllowScreenshotsPreference): Result<Unit>
    fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference>

    suspend fun clearPreferences(): Result<Unit>
}
