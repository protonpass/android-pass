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
import kotlinx.coroutines.runBlocking
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

    override fun setBiometricLockState(state: BiometricLockState): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setBiometricLock(state.value().toBooleanPrefProto())
                        .build()
                }
            }
        }

    override fun getBiometricLockState(): Flow<BiometricLockState> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                BiometricLockState.from(fromBooleanPrefProto(preferences.biometricLock))
            }

    override fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> = runCatching {
        runBlocking {
            inMemoryPreferences.set(HasAuthenticated::class.java.name, state.value())
            dataStore.updateData {
                it.toBuilder()
                    .setHasAuthenticatedWithBiometry(state.value().toBooleanPrefProto())
                    .build()
            }
        }
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                inMemoryPreferences.get<Boolean>(HasAuthenticated::class.java.name)
                    ?.let { HasAuthenticated.from(it) }
                    ?: HasAuthenticated.from(fromBooleanPrefProto(preferences.hasAuthenticatedWithBiometry))
            }

    override fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setCompletedOnboarding(state.value().toBooleanPrefProto())
                        .build()
                }
            }
        }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasCompletedOnBoarding.from(fromBooleanPrefProto(preferences.completedOnboarding))
            }

    override fun setThemePreference(theme: ThemePreference): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setThemeValue(theme.value())
                        .build()
                }
            }
        }

    override fun getThemePreference(): Flow<ThemePreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                ThemePreference.from(preferences.themeValue)
            }

    override fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setHasDismissedAutofillBanner(state.value().toBooleanPrefProto())
                        .build()
                }
            }
        }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasDismissedAutofillBanner.from(fromBooleanPrefProto(preferences.hasDismissedAutofillBanner))
            }

    override fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setCopyTotpToClipboardEnabled(state.value().toBooleanPrefProto())
                        .build()
                }
            }
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

    override fun setClearClipboardPreference(
        clearClipboard: ClearClipboardPreference
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                it.toBuilder()
                    .setClearClipboardAfterValue(clearClipboard.value())
                    .build()
            }
        }
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                ClearClipboardPreference.from(preferences.clearClipboardAfterValue)
            }

    override fun setUseFaviconsPreference(
        useFavicons: UseFaviconsPreference
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                it.toBuilder()
                    .setUseFavicons(useFavicons.value().toBooleanPrefProto())
                    .build()
            }
        }
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

    override fun setAppLockTimePreference(
        preference: AppLockTimePreference
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                it.toBuilder()
                    .setLockApp(preference.toProto())
                    .build()
            }
        }
    }

    override fun getAppLockTimePreference(): Flow<AppLockTimePreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.lockApp.toValue(default = AppLockTimePreference.InTwoMinutes)
            }

    override fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setAppLockType(preference.toProto())
                        .build()
                }
            }
        }

    override fun getAppLockTypePreference(): Flow<AppLockTypePreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.appLockType.toValue(default = AppLockTypePreference.Biometrics)
            }

    override fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setBiometricSystemLock(preference.value().toBooleanPrefProto())
                        .build()
                }
            }
        }

    override fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                BiometricSystemLockPreference.from(
                    fromBooleanPrefProto(
                        pref = preferences.biometricSystemLock,
                        default = true
                    )
                )
            }

    override fun setPasswordGenerationPreference(
        preference: PasswordGenerationPreference
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                it.toBuilder()
                    .setPasswordGeneration(preference.toProto())
                    .build()
            }
        }
    }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                preferences.passwordGeneration.toValue()
            }


    override fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .setHasDismissedTrialBanner(state.value().toBooleanPrefProto())
                        .build()
                }
            }
        }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                HasDismissedTrialBanner.from(fromBooleanPrefProto(preferences.hasDismissedTrialBanner))
            }

    override fun setAllowScreenshotsPreference(
        preference: AllowScreenshotsPreference
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                it.toBuilder()
                    .setAllowScreenshots(preference.value().toBooleanPrefProto())
                    .build()
            }
        }
    }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> =
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences ->
                AllowScreenshotsPreference.from(
                    fromBooleanPrefProto(preferences.allowScreenshots)
                )
            }

    override fun clearPreferences(): Result<Unit> =
        runCatching {
            runBlocking {
                dataStore.updateData {
                    it.toBuilder()
                        .clear()
                        .build()
                }
            }
        }

    private fun FlowCollector<UserPreferences>.handleExceptions(
        exception: Throwable
    ) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            runBlocking { emit(UserPreferences.getDefaultInstance()) }
        } else {
            throw exception
        }
    }
}
