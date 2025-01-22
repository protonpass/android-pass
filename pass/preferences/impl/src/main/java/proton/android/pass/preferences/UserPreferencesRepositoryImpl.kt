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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.core.domain.entity.UserId
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import proton.android.pass.preferences.sentinel.SentinelStatusPreference
import proton.android.pass.preferences.settings.SettingsDisplayAutofillPinningPreference
import proton.android.pass.preferences.settings.SettingsDisplayUsernameFieldPreference
import proton.android.pass.preferences.simplelogin.SimpleLoginSyncStatusPreference
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
    private val inMemoryPreferences: InMemoryPreferences,
    private val appConfig: AppConfig
) : UserPreferencesRepository {

    override fun setAppLockState(state: AppLockState): Result<Unit> = setPreference {
        it.setBiometricLock(state.value().toBooleanPrefProto())
    }

    override fun getAppLockState(): Flow<AppLockState> = getPreference {
        AppLockState.from(fromBooleanPrefProto(it.biometricLock))
    }

    override fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> = setPreference {
        inMemoryPreferences.set(HasAuthenticated::class.java.name, state.value())
        it.setHasAuthenticatedWithBiometry(state.value().toBooleanPrefProto())
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> = getPreference {
        inMemoryPreferences.get<Boolean>(HasAuthenticated::class.java.name)
            ?.let { value -> HasAuthenticated.from(value) }
            ?: HasAuthenticated.from(fromBooleanPrefProto(it.hasAuthenticatedWithBiometry))
    }

    override fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> = setPreference {
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

    override fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> = setPreference {
        it.setHasDismissedAutofillBanner(state.value().toBooleanPrefProto())
    }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> = getPreference {
        HasDismissedAutofillBanner.from(fromBooleanPrefProto(it.hasDismissedAutofillBanner))
    }

    override fun setHasDismissedNotificationBanner(state: HasDismissedNotificationBanner): Result<Unit> =
        setPreference {
            it.setHasDismissedNotificationBanner(state.value().toBooleanPrefProto())
        }

    override fun getHasDismissedNotificationBanner(): Flow<HasDismissedNotificationBanner> = getPreference {
        HasDismissedNotificationBanner.from(fromBooleanPrefProto(it.hasDismissedNotificationBanner))
    }

    override fun setHasDismissedSLSyncBanner(state: HasDismissedSLSyncBanner): Result<Unit> =
        setPreference { it.setHasDismissedSlSyncBanner(state.value().toBooleanPrefProto()) }

    override fun getHasDismissedSLSyncBanner(): Flow<HasDismissedSLSyncBanner> = getPreference {
        HasDismissedSLSyncBanner.from(fromBooleanPrefProto(it.hasDismissedSlSyncBanner))
    }

    override fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> = setPreference {
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

    override fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit> = setPreference {
        it.setClearClipboardAfterValue(clearClipboard.value())
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> = getPreference {
        ClearClipboardPreference.from(it.clearClipboardAfterValue)
    }

    override fun setUseFaviconsPreference(useFavicons: UseFaviconsPreference): Result<Unit> =
        setPreference { it.setUseFavicons(useFavicons.value().toBooleanPrefProto()) }

    override fun getUseFaviconsPreference(): Flow<UseFaviconsPreference> = getPreference {
        UseFaviconsPreference.from(
            fromBooleanPrefProto(
                pref = it.useFavicons,
                default = true
            )
        )
    }

    override fun setAppLockTimePreference(preference: AppLockTimePreference): Result<Unit> =
        setPreference { it.setLockApp(preference.toProto()) }

    override fun getAppLockTimePreference(): Flow<AppLockTimePreference> = getPreference {
        it.lockApp.toValue(default = AppLockTimePreference.InTwoMinutes)
    }

    override fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> =
        setPreference { it.setAppLockType(preference.toProto()) }

    override fun getAppLockTypePreference(): Flow<AppLockTypePreference> = getPreference {
        it.appLockType.toValue(default = AppLockTypePreference.None)
    }

    override fun setBiometricSystemLockPreference(preference: BiometricSystemLockPreference): Result<Unit> =
        setPreference {
            it.setBiometricSystemLock(preference.value().toBooleanPrefProto())
        }

    override fun getBiometricSystemLockPreference(): Flow<BiometricSystemLockPreference> = getPreference {
        BiometricSystemLockPreference.from(
            fromBooleanPrefProto(
                pref = it.biometricSystemLock,
                default = true
            )
        )
    }

    override fun setPasswordGenerationPreference(preference: PasswordGenerationPreference): Result<Unit> =
        setPreference { it.setPasswordGeneration(preference.toProto()) }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> = getPreference {
        it.passwordGeneration.toValue()
    }

    override fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> = setPreference {
        it.setHasDismissedTrialBanner(state.value().toBooleanPrefProto())
    }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> = getPreference {
        HasDismissedTrialBanner.from(fromBooleanPrefProto(it.hasDismissedTrialBanner))
    }

    override fun setAllowScreenshotsPreference(preference: AllowScreenshotsPreference): Result<Unit> = setPreference {
        it.setAllowScreenshots(preference.value().toBooleanPrefProto())
    }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> = getPreference {
        val value = fromBooleanPrefProto(
            pref = it.allowScreenshots,
            default = appConfig.allowScreenshotsDefaultValue
        )
        AllowScreenshotsPreference.from(value)
    }

    override fun setDefaultVault(userId: UserId, shareId: ShareId): Result<Unit> = setPreference {
        it.putDefaultVaultPerUser(userId.id, shareId.id)
    }

    override fun getDefaultVault(userId: UserId): Flow<Option<String>> = combine(
        getPreference { it.defaultVaultPerUserMap },
        getPreference { it.defaultVault }
    ) { defaultVaultPerUser, defaultVault ->
        if (defaultVaultPerUser.isEmpty() && defaultVault.isNotBlank()) {
            setPreference { it.putDefaultVaultPerUser(userId.id, defaultVault) }
            defaultVault.toOption()
        } else {
            defaultVaultPerUser[userId.id].toOption()
        }
    }

    override fun tryClearPreferences(): Result<Unit> = runBlocking { clearPreferences() }

    override suspend fun clearPreferences(): Result<Unit> = setPreferenceSuspend { it.clear() }

    override fun setSentinelStatusPreference(preference: SentinelStatusPreference): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            preference.value
                .toBooleanPrefProto()
                .let(userPreferencesBuilder::setSentinelStatus)
        }

    override fun observeSentinelStatusPreference(): Flow<SentinelStatusPreference> = getPreference { userPreferences ->
        fromBooleanPrefProto(userPreferences.sentinelStatus)
            .let(SentinelStatusPreference::from)
    }

    override fun setMonitorStatusPreference(preference: MonitorStatusPreference): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            preference.toProto()
                .let(userPreferencesBuilder::setMonitorStatus)
        }

    override fun observeMonitorStatusPreference(): Flow<MonitorStatusPreference> = getPreference { userPreferences ->
        userPreferences.monitorStatus.toValue()
    }

    override fun setSimpleLoginSyncStatusPreference(preference: SimpleLoginSyncStatusPreference): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            preference.value
                .toBooleanPrefProto()
                .let(userPreferencesBuilder::setSimpleLoginSyncStatus)
        }

    override fun observeSimpleLoginSyncStatusPreference(): Flow<SimpleLoginSyncStatusPreference> =
        getPreference { userPreferences ->
            fromBooleanPrefProto(
                pref = userPreferences.simpleLoginSyncStatus,
                default = true
            ).let(SimpleLoginSyncStatusPreference::from)
        }

    override fun setAliasTrashDialogStatusPreference(preference: AliasTrashDialogStatusPreference): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            preference.value
                .toBooleanPrefProto()
                .let(userPreferencesBuilder::setAliasTrashDialogStatus)
        }

    override fun observeAliasTrashDialogStatusPreference(): Flow<AliasTrashDialogStatusPreference> =
        getPreference { userPreferences ->
            fromBooleanPrefProto(
                pref = userPreferences.aliasTrashDialogStatus,
                default = false
            ).let(AliasTrashDialogStatusPreference::from)
        }

    override fun setDisplayUsernameFieldPreference(preference: SettingsDisplayUsernameFieldPreference): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            preference.value
                .toBooleanPrefProto()
                .let(userPreferencesBuilder::setDisplayUsernameField)
        }

    override fun observeDisplayUsernameFieldPreference(): Flow<SettingsDisplayUsernameFieldPreference> =
        getPreference { userPreferences ->
            fromBooleanPrefProto(
                pref = userPreferences.displayUsernameField,
                default = false
            ).let(SettingsDisplayUsernameFieldPreference::from)
        }

    override fun setDisplayAutofillPinningPreference(
        preference: SettingsDisplayAutofillPinningPreference
    ): Result<Unit> = setPreference { userPreferencesBuilder ->
        preference.value
            .toBooleanPrefProto()
            .let(userPreferencesBuilder::setDisplayAutofillPinning)
    }

    override fun observeDisplayAutofillPinningPreference(): Flow<SettingsDisplayAutofillPinningPreference> =
        getPreference { userPreferences ->
            fromBooleanPrefProto(
                pref = userPreferences.displayAutofillPinning,
                default = false
            ).let(SettingsDisplayAutofillPinningPreference::from)
        }

    override fun observeDisplayFileAttachmentsOnboarding(): Flow<DisplayFileAttachmentsBanner> =
        getPreference { userPreferences ->
            when (userPreferences.displayFileAttachmentsOnboarding) {
                BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> DisplayFileAttachmentsBanner.Display
                BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> DisplayFileAttachmentsBanner.NotDisplay
                BooleanPrefProto.BOOLEAN_PREFERENCE_UNSPECIFIED,
                BooleanPrefProto.UNRECOGNIZED,
                null -> DisplayFileAttachmentsBanner.Unknown
            }
        }

    override fun setDisplayFileAttachmentsOnboarding(value: DisplayFileAttachmentsBanner): Result<Unit> =
        setPreference { userPreferencesBuilder ->
            value.value()
                .toBooleanPrefProto()
                .let(userPreferencesBuilder::setDisplayFileAttachmentsOnboarding)
        }

    override fun setUseDigitalAssetLinksPreference(preference: UseDigitalAssetLinksPreference): Result<Unit> =
        setPreference { it.setUseDigitalAssetLinks(preference.value().toBooleanPrefProto()) }

    override fun observeUseDigitalAssetLinksPreference(): Flow<UseDigitalAssetLinksPreference> = getPreference {
        UseDigitalAssetLinksPreference.from(
            fromBooleanPrefProto(
                pref = it.useDigitalAssetLinks,
                default = true
            )
        )
    }

    override fun setDisplayFeatureDiscoverBanner(
        feature: FeatureDiscoveryFeature,
        preference: FeatureDiscoveryBannerPreference
    ): Result<Unit> = setPreference {
        it.putFeatureDiscoveryBanners(feature.id, preference.value.toBooleanPrefProto())
    }

    override fun observeDisplayFeatureDiscoverBanner(
        feature: FeatureDiscoveryFeature
    ): Flow<FeatureDiscoveryBannerPreference> = getPreference {
        val booleanPref = it.featureDiscoveryBannersMap[feature.id]
            ?: BooleanPrefProto.BOOLEAN_PREFERENCE_UNSPECIFIED
        when (booleanPref) {
            BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> FeatureDiscoveryBannerPreference.Display
            BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> FeatureDiscoveryBannerPreference.NotDisplay
            BooleanPrefProto.BOOLEAN_PREFERENCE_UNSPECIFIED,
            BooleanPrefProto.UNRECOGNIZED -> FeatureDiscoveryBannerPreference.Unknown
        }
    }

    private fun setPreference(mapper: suspend (UserPreferences.Builder) -> UserPreferences.Builder): Result<Unit> =
        runBlocking {
            setPreferenceSuspend(mapper)
        }

    private suspend fun setPreferenceSuspend(
        mapper: suspend (UserPreferences.Builder) -> UserPreferences.Builder
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                mapper(it.toBuilder()).build()
            }
        }
    }

    private fun <T> getPreference(mapper: (UserPreferences) -> T): Flow<T> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> mapper(settings) }

    private fun FlowCollector<UserPreferences>.handleExceptions(exception: Throwable) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            runBlocking { emit(UserPreferences.getDefaultInstance()) }
        } else {
            throw exception
        }
    }
}
