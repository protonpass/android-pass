package me.proton.android.pass.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val DEFAULT_BIOMETRIC_LOCK = false

class PreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceRepository {

    override fun setBiometricLockState(state: BiometricLockState): Flow<Unit> = flow {
        dataStore.edit { preferences ->
            preferences[PassPreferences.BIOMETRIC_LOCK] = when (state) {
                BiometricLockState.Enabled -> true
                BiometricLockState.Disabled -> false
            }
        }
    }

    override fun getBiometricLockState(): Flow<BiometricLockState> =
        dataStore.data
            .map { preferences ->
                val value = preferences[PassPreferences.BIOMETRIC_LOCK] ?: DEFAULT_BIOMETRIC_LOCK
                if (value) {
                    BiometricLockState.Enabled
                } else {
                    BiometricLockState.Disabled
                }
            }

}
