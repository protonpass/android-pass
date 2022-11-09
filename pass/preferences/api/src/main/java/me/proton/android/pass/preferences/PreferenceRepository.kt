package me.proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun setBiometricLockState(state: BiometricLockState): Flow<Unit>
    fun getBiometricLockState(): Flow<BiometricLockState>

    fun setHasAuthenticated(state: HasAuthenticated): Flow<Unit>
    fun getHasAuthenticated(): Flow<HasAuthenticated>

    fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Flow<Unit>
    fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding>

    fun setThemePreference(theme: ThemePreference): Flow<Unit>
    fun getThemePreference(): Flow<ThemePreference>

    fun clearPreferences(): Flow<Unit>
}
