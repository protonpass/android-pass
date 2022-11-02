package me.proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun setBiometricLockState(state: BiometricLockState): Flow<Unit>
    fun getBiometricLockState(): Flow<BiometricLockState>
}
