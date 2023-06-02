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

    suspend fun setAppLockPreference(preference: AppLockPreference): Result<Unit>
    fun getAppLockPreference(): Flow<AppLockPreference>

    suspend fun setPasswordGenerationPreference(preference: PasswordGenerationPreference): Result<Unit>
    fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference>

    suspend fun clearPreferences(): Result<Unit>
}
