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

    override fun setBiometricLockState(state: BiometricLockState): Result<Unit> = setPreference {
        it.setBiometricLock(state.value().toBooleanPrefProto())
    }

    override fun getBiometricLockState(): Flow<BiometricLockState> = getPreference {
        BiometricLockState.from(fromBooleanPrefProto(it.biometricLock))
    }

    override fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> = setPreference {
        inMemoryPreferences.set(HasAuthenticated::class.java.name, state.value())
        it.setHasAuthenticatedWithBiometry(state.value().toBooleanPrefProto())
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> = getPreference {
        inMemoryPreferences.get<Boolean>(HasAuthenticated::class.java.name)
            ?.let { HasAuthenticated.from(it) }
            ?: HasAuthenticated.from(fromBooleanPrefProto(it.hasAuthenticatedWithBiometry))
    }

    override fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> =
        setPreference {
            it.setCompletedOnboarding(state.value().toBooleanPrefProto())
        }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> = getPreference {
        HasCompletedOnBoarding.from(fromBooleanPrefProto(it.completedOnboarding))
    }

    override fun setThemePreference(theme: ThemePreference): Result<Unit> = setPreference {
        it.setThemeValue(theme.value())
    }

    override fun getThemePreference(): Flow<ThemePreference> = getPreference {
        ThemePreference.from(it.themeValue)
    }

    override fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> =
        setPreference {
            it.setHasDismissedAutofillBanner(state.value().toBooleanPrefProto())
        }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> = getPreference {
        HasDismissedAutofillBanner.from(fromBooleanPrefProto(it.hasDismissedAutofillBanner))
    }

    override fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> =
        setPreference {
            it.setCopyTotpToClipboardEnabled(state.value().toBooleanPrefProto())
        }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> = getPreference {
        CopyTotpToClipboard.from(
            fromBooleanPrefProto(
                pref = it.copyTotpToClipboardEnabled,
                default = true
            )
        )
    }

    override fun setClearClipboardPreference(
        clearClipboard: ClearClipboardPreference
    ): Result<Unit> = setPreference {
        it.setClearClipboardAfterValue(clearClipboard.value())
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> = getPreference {
        ClearClipboardPreference.from(it.clearClipboardAfterValue)
    }

    override fun setUseFaviconsPreference(
        useFavicons: UseFaviconsPreference
    ): Result<Unit> = setPreference { it.setUseFavicons(useFavicons.value().toBooleanPrefProto()) }

    override fun getUseFaviconsPreference(): Flow<UseFaviconsPreference> = getPreference {
        UseFaviconsPreference.from(
            fromBooleanPrefProto(
                pref = it.useFavicons,
                default = true
            )
        )
    }

    override fun setAppLockTimePreference(
        preference: AppLockTimePreference
    ): Result<Unit> = setPreference { it.setLockApp(preference.toProto()) }

    override fun getAppLockTimePreference(): Flow<AppLockTimePreference> = getPreference {
        it.lockApp.toValue(default = AppLockTimePreference.InTwoMinutes)
    }

    override fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> =
        setPreference { it.setAppLockType(preference.toProto()) }

    override fun getAppLockTypePreference(): Flow<AppLockTypePreference> = getPreference {
        it.appLockType.toValue(default = AppLockTypePreference.Biometrics)
    }

    override fun setBiometricSystemLockPreference(
        preference: BiometricSystemLockPreference
    ): Result<Unit> = setPreference {
        it.setBiometricSystemLock(preference.value().toBooleanPrefProto())
    }

    override fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference> =
        getPreference {
            BiometricSystemLockPreference.from(
                fromBooleanPrefProto(
                    pref = it.biometricSystemLock,
                    default = true
                )
            )
        }

    override fun setPasswordGenerationPreference(
        preference: PasswordGenerationPreference
    ): Result<Unit> = setPreference { it.setPasswordGeneration(preference.toProto()) }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> =
        getPreference {
            it.passwordGeneration.toValue()
        }

    override fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> =
        setPreference {
            it.setHasDismissedTrialBanner(state.value().toBooleanPrefProto())
        }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> = getPreference {
        HasDismissedTrialBanner.from(fromBooleanPrefProto(it.hasDismissedTrialBanner))
    }

    override fun setAllowScreenshotsPreference(
        preference: AllowScreenshotsPreference
    ): Result<Unit> = setPreference {
        it.setAllowScreenshots(preference.value().toBooleanPrefProto())
    }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> = getPreference {
        AllowScreenshotsPreference.from(fromBooleanPrefProto(it.allowScreenshots))
    }

    override fun clearPreferences(): Result<Unit> = setPreference { it.clear() }

    private fun setPreference(
        mapper: (UserPreferences.Builder) -> UserPreferences.Builder
    ): Result<Unit> = runCatching {
        runBlocking {
            dataStore.updateData {
                mapper(it.toBuilder()).build()
            }
        }
        return@runCatching
    }

    private fun <T> getPreference(mapper: (UserPreferences) -> T): Flow<T> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> mapper(settings) }

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
