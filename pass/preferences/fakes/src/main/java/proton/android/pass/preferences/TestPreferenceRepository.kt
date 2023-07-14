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
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class TestPreferenceRepository @Inject constructor() : UserPreferencesRepository {

    private val appLockState =
        MutableStateFlow<AppLockState>(AppLockState.Disabled)
    private val themePreference = MutableStateFlow(ThemePreference.Dark)

    private val hasAuthenticated =
        MutableStateFlow<HasAuthenticated>(HasAuthenticated.Authenticated)
    private val hasCompletedOnBoarding =
        MutableStateFlow<HasCompletedOnBoarding>(HasCompletedOnBoarding.Completed)
    private val hasDismissedAutofillBanner =
        MutableStateFlow<HasDismissedAutofillBanner>(HasDismissedAutofillBanner.Dismissed)
    private val hasDismissedTrialBanner =
        MutableStateFlow<HasDismissedTrialBanner>(HasDismissedTrialBanner.Dismissed)
    private val copyTotpToClipboard =
        MutableStateFlow<CopyTotpToClipboard>(CopyTotpToClipboard.NotEnabled)
    private val clearClipboardPreference = MutableStateFlow(ClearClipboardPreference.Never)
    private val useFaviconsPreference =
        MutableStateFlow<UseFaviconsPreference>(UseFaviconsPreference.Disabled)
    private val allowScreenshotsPreference =
        MutableStateFlow<AllowScreenshotsPreference>(AllowScreenshotsPreference.Disabled)
    private val appLockTimePreference = MutableStateFlow(AppLockTimePreference.InFourHours)
    private val appLockTypePreference = MutableStateFlow(AppLockTypePreference.Biometrics)
    private val biometricSystemLockPreference: MutableStateFlow<BiometricSystemLockPreference> =
        MutableStateFlow(BiometricSystemLockPreference.Enabled)
    private val passwordGenerationPreference = MutableStateFlow(
        PasswordGenerationPreference(
            mode = PasswordGenerationMode.Words,
            randomPasswordLength = 12,
            randomHasSpecialCharacters = false,
            randomHasCapitalLetters = false,
            randomIncludeNumbers = false,
            wordsCount = 4,
            wordsSeparator = WordSeparator.Hyphen,
            wordsCapitalise = false,
            wordsIncludeNumbers = false
        )
    )

    override fun setAppLockState(state: AppLockState): Result<Unit> {
        appLockState.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getAppLockState(): Flow<AppLockState> = appLockState

    override fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> {
        hasAuthenticated.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> = hasAuthenticated

    override fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> {
        hasCompletedOnBoarding.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> = hasCompletedOnBoarding

    override fun setThemePreference(theme: ThemePreference): Result<Unit> {
        themePreference.tryEmit(theme)
        return Result.success(Unit)
    }

    override fun getThemePreference(): Flow<ThemePreference> = themePreference

    override fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> {
        hasDismissedAutofillBanner.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        hasDismissedAutofillBanner

    override fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> {
        hasDismissedTrialBanner.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> =
        hasDismissedTrialBanner

    override fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> {
        copyTotpToClipboard.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> = copyTotpToClipboard

    override fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit> {
        clearClipboardPreference.tryEmit(clearClipboard)
        return Result.success(Unit)
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> =
        clearClipboardPreference

    override fun setUseFaviconsPreference(useFavicons: UseFaviconsPreference): Result<Unit> {
        useFaviconsPreference.tryEmit(useFavicons)
        return Result.success(Unit)
    }

    override fun getUseFaviconsPreference(): Flow<UseFaviconsPreference> = useFaviconsPreference

    override fun setAppLockTimePreference(preference: AppLockTimePreference): Result<Unit> {
        appLockTimePreference.tryEmit(preference)
        return Result.success(Unit)
    }

    override fun getAppLockTimePreference(): Flow<AppLockTimePreference> = appLockTimePreference
    override fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> {
        appLockTypePreference.tryEmit(preference)
        return Result.success(Unit)
    }

    override fun getAppLockTypePreference(): Flow<AppLockTypePreference> = appLockTypePreference

    override fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit> {
        biometricSystemLockPreference.tryEmit(preference)
        return Result.success(Unit)
    }

    override fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference> =
        biometricSystemLockPreference

    override fun setPasswordGenerationPreference(
        preference: PasswordGenerationPreference
    ): Result<Unit> {
        passwordGenerationPreference.tryEmit(preference)
        return Result.success(Unit)
    }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> =
        passwordGenerationPreference

    override fun setAllowScreenshotsPreference(
        preference: AllowScreenshotsPreference
    ): Result<Unit> {
        allowScreenshotsPreference.tryEmit(preference)
        return Result.success(Unit)
    }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> =
        allowScreenshotsPreference

    override fun clearPreferences(): Result<Unit> = Result.success(Unit)

}
