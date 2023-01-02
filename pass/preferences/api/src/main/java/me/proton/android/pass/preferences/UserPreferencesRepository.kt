package me.proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow

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

    suspend fun clearPreferences(): Result<Unit>
}
