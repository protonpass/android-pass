package me.proton.android.pass.preferences

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

class TestPreferenceRepository : PreferenceRepository {

    private val biometricLockState = MutableSharedFlow<BiometricLockState>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val themePreference = MutableSharedFlow<ThemePreference>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    override fun setBiometricLockState(state: BiometricLockState): Flow<Unit> {
        biometricLockState.tryEmit(state)
        return flowOf(Unit)
    }

    override fun getBiometricLockState(): Flow<BiometricLockState> = biometricLockState

    override fun setThemePreference(theme: ThemePreference): Flow<Unit> {
        themePreference.tryEmit(theme)
        return flowOf(Unit)
    }

    override fun getThemePreference(): Flow<ThemePreference> = themePreference

}
