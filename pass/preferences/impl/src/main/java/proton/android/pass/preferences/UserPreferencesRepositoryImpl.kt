package proton.android.pass.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) : UserPreferencesRepository {

    override suspend fun setBiometricLockState(state: BiometricLockState): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setBiometricLock(state.value())
                    .build()
            }
            return@runCatching
        }

    override fun getBiometricLockState(): Flow<BiometricLockState> =
        dataStore.data
            .map { preferences ->
                BiometricLockState.from(preferences.biometricLock)
            }

    override suspend fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHasAuthenticatedWithBiometry(state.value())
                    .build()
            }
            return@runCatching
        }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> =
        dataStore.data
            .map { preferences ->
                HasAuthenticated.from(preferences.hasAuthenticatedWithBiometry)
            }

    override suspend fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setCompletedOnboarding(state.value())
                    .build()
            }
            return@runCatching
        }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> =
        dataStore.data
            .map { preferences ->
                HasCompletedOnBoarding.from(preferences.completedOnboarding)
            }

    override suspend fun setThemePreference(theme: ThemePreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setThemeValue(theme.value())
                    .build()
            }
            return@runCatching
        }

    override fun getThemePreference(): Flow<ThemePreference> =
        dataStore.data
            .map { preferences ->
                ThemePreference.from(preferences.themeValue)
            }

    override suspend fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHasDismissedAutofillBanner(state.value())
                    .build()
            }
            return@runCatching
        }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        dataStore.data
            .map { preferences ->
                HasDismissedAutofillBanner.from(preferences.hasDismissedAutofillBanner)
            }

    override suspend fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setCopyTotpToClipboardEnabled(state.value())
                    .build()
            }
            return@runCatching
        }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> =
        dataStore.data
            .map { preferences ->
                CopyTotpToClipboard.from(preferences.copyTotpToClipboardEnabled)
            }

    override suspend fun clearPreferences(): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .clear()
                    .build()
            }
            return@runCatching
        }
}
