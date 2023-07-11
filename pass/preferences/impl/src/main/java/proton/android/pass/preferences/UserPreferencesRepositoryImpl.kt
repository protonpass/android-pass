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

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
    private val inMemoryPreferences: InMemoryPreferences
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
            inMemoryPreferences.set(HasAuthenticated::class.java.name, state.value())
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
                inMemoryPreferences.get<Boolean>(HasAuthenticated::class.java.name)
                    ?.let { HasAuthenticated.from(it) }
                    ?: HasAuthenticated.from(fromBooleanPrefProto(preferences.hasAuthenticatedWithBiometry))
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

    override suspend fun setAppLockTimePreference(
        preference: AppLockTimePreference
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setLockApp(preference.toProto())
                .build()
        }
        return@runCatching
    }

    override fun getAppLockTimePreference(): Flow<AppLockTimePreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.lockApp.toValue(default = AppLockTimePreference.InTwoMinutes)
            }

    override suspend fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setAppLockType(preference.toProto())
                    .build()
            }
            return@runCatching
        }

    override fun getAppLockTypePreference(): Flow<AppLockTypePreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.appLockType.toValue(default = AppLockTypePreference.Biometrics)
            }

    override suspend fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setBiometricSystemLock(preference.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                BiometricSystemLockPreference.from(fromBooleanPrefProto(preferences.biometricSystemLock))
            }


    override suspend fun setPasswordGenerationPreference(
        preference: PasswordGenerationPreference
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setPasswordGeneration(preference.toProto())
                .build()
        }
        return@runCatching
    }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.passwordGeneration.toValue()
            }


    override suspend fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHasDismissedTrialBanner(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasDismissedTrialBanner.from(fromBooleanPrefProto(preferences.hasDismissedTrialBanner))
            }

    override suspend fun setAllowScreenshotsPreference(state: AllowScreenshotsPreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setAllowScreenshots(state.value().toBooleanPrefProto())
                    .build()
            }
            return@runCatching
        }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                AllowScreenshotsPreference.from(
                    fromBooleanPrefProto(preferences.allowScreenshots)
                )
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
