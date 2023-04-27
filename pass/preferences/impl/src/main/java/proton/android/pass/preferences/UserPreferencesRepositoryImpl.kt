package proton.android.pass.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) : UserPreferencesRepository {

    override suspend fun setBiometricLockState(state: BiometricLockState): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setBiometricLock(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getBiometricLockState(): Flow<BiometricLockState> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                BiometricLockState.from(fromBooleanPrefProto(preferences.biometricLock))
            }

    override suspend fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHasAuthenticatedWithBiometry(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasAuthenticated.from(fromBooleanPrefProto(preferences.hasAuthenticatedWithBiometry))
            }

    override suspend fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setCompletedOnboarding(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasCompletedOnBoarding.from(fromBooleanPrefProto(preferences.completedOnboarding))
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
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                ThemePreference.from(preferences.themeValue)
            }

    override suspend fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHasDismissedAutofillBanner(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasDismissedAutofillBanner.from(fromBooleanPrefProto(preferences.hasDismissedAutofillBanner))
            }

    override suspend fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setCopyTotpToClipboardEnabled(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                CopyTotpToClipboard.from(
                    fromBooleanPrefProto(
                        pref = preferences.copyTotpToClipboardEnabled,
                        default = true
                    )
                )
            }

    override suspend fun setClearClipboardPreference(
        clearClipboard: ClearClipboardPreference
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setClearClipboardAfterValue(clearClipboard.value())
                .build()
        }
        return@runCatching
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                ClearClipboardPreference.from(preferences.clearClipboardAfterValue)
            }

    override suspend fun setUseFaviconsPreference(
        useFavicons: UseFaviconsPreference
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setUseFavicons(useFavicons.value().toBooleanPrefProto())
                .build()
        }
        return@runCatching
    }

    override fun getUseFaviconsPreference(): Flow<UseFaviconsPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                UseFaviconsPreference.from(
                    fromBooleanPrefProto(
                        pref = preferences.useFavicons,
                        default = true
                    )
                )
            }

    override suspend fun setAppLockPreference(
        preference: AppLockPreference
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setLockApp(preference.toProto())
                .build()
        }
        return@runCatching
    }

    override fun getAppLockPreference(): Flow<AppLockPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.lockApp.toValue(default = AppLockPreference.InTwoMinutes)
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

    private suspend fun FlowCollector<UserPreferences>.handleExceptions(
        exception: Throwable
    ) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            emit(UserPreferences.getDefaultInstance())
        } else {
            throw exception
        }
    }
}
