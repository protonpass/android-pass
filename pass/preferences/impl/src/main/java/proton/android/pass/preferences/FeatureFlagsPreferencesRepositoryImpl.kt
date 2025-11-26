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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.proton.android.pass.preferences.BoolFlagPrefProto
import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import proton.android.pass.preferences.FeatureFlag.EXTRA_LOGGING
import proton.android.pass.preferences.FeatureFlag.PASS_GROUP_SHARE
import proton.android.pass.preferences.FeatureFlag.PASS_HIDE_SHOW_VAULT
import proton.android.pass.preferences.FeatureFlag.RENAME_ADMIN_TO_MANAGER
import proton.android.pass.preferences.FeatureFlag.PASS_ALLOW_NO_VAULT
import proton.android.pass.preferences.FeatureFlag.PASS_USER_EVENTS_V1
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class FeatureFlagsPreferencesRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val featureFlagManager: FeatureFlagRepository,
    private val dataStore: DataStore<FeatureFlagsPreferences>
) : FeatureFlagsPreferencesRepository {

    override fun <T> get(featureFlag: FeatureFlag): Flow<T> = when (featureFlag) {
        AUTOFILL_DEBUG_MODE -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { autofillDebugModeEnabled.value }

        EXTRA_LOGGING -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { extraLoggingEnabled.value }

        RENAME_ADMIN_TO_MANAGER -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { renameAdminToManagerEnabled.value }

        PASS_HIDE_SHOW_VAULT -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { passHideShowVaultEnabled.value }

        PASS_ALLOW_NO_VAULT -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { passAllowNoVault.value }

        PASS_USER_EVENTS_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { passUserEventsV1Enabled.value }

        PASS_GROUP_SHARE -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { groupsEnabled.value }
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> = when (featureFlag) {
        AUTOFILL_DEBUG_MODE -> setFeatureFlag {
            autofillDebugModeEnabled = boolFlagPrefProto(value)
        }

        EXTRA_LOGGING -> setFeatureFlag {
            extraLoggingEnabled = boolFlagPrefProto(value)
        }

        RENAME_ADMIN_TO_MANAGER -> setFeatureFlag {
            renameAdminToManagerEnabled = boolFlagPrefProto(value)
        }

        PASS_HIDE_SHOW_VAULT -> setFeatureFlag {
            passHideShowVaultEnabled = boolFlagPrefProto(value)
        }

        PASS_ALLOW_NO_VAULT -> setFeatureFlag {
            passAllowNoVault = boolFlagPrefProto(value)
        }

        PASS_USER_EVENTS_V1 -> setFeatureFlag {
            passUserEventsV1Enabled = boolFlagPrefProto(value)
        }

        PASS_GROUP_SHARE -> setFeatureFlag {
            groupsEnabled = boolFlagPrefProto(value)
        }
    }

    private fun <T> getFeatureFlag(
        key: String?,
        defaultValue: Boolean,
        prefGetter: FeatureFlagsPreferences.() -> BooleanPrefProto
    ): Flow<T> = if (key != null) {
        accountManager.getPrimaryUserId()
            .flatMapLatest { userId ->
                featureFlagManager.observe(
                    userId = userId,
                    featureId = FeatureId(id = key)
                )
            }
            .flatMapLatest { featureFlag ->
                dataStore.data
                    .catch { exception -> handleExceptions(exception) }
                    .map { preferences ->
                        fromBooleanPrefProto(
                            pref = prefGetter(preferences),
                            default = featureFlag?.value ?: defaultValue
                        ) as T
                    }
            }
    } else {
        dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map {
                fromBooleanPrefProto(prefGetter(it)) as T
            }
    }

    private fun setFeatureFlag(setter: FeatureFlagsPreferences.Builder.() -> Unit) = runCatching {
        runBlocking {
            dataStore.updateData { prefs ->
                val a = prefs.toBuilder()
                setter(a)
                a.build()
            }
        }
        return@runCatching
    }

    private fun <T> boolFlagPrefProto(value: T?): BoolFlagPrefProto {
        val builder = BoolFlagPrefProto.newBuilder()
        value?.let { builder.value = (it as Boolean).toBooleanPrefProto() }
        return builder.build()
    }

    private suspend fun FlowCollector<FeatureFlagsPreferences>.handleExceptions(exception: Throwable) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            emit(FeatureFlagsPreferences.getDefaultInstance())
        } else {
            throw exception
        }
    }

    override fun observeForAllUsers(featureFlag: FeatureFlag): Flow<Boolean> = accountManager.getAccounts()
        .flatMapLatest { accounts ->
            combine(
                accounts.map { account ->
                    observeIsFeatureEnabled(featureFlag, account.userId)
                }
            ) { areFeaturesEnabled ->
                areFeaturesEnabled.any { it }
            }
        }

    private fun observeIsFeatureEnabled(featureFlag: FeatureFlag, userId: UserId?): Flow<Boolean> =
        featureFlag.key?.let { featureFlagKey ->
            featureFlagManager.observe(
                userId = userId,
                featureId = FeatureId(id = featureFlagKey)
            ).flatMapLatest { remoteFeatureFlag ->
                dataStore.data
                    .catch { exception -> handleExceptions(exception) }
                    .map { preferences ->
                        fromBooleanPrefProto(
                            pref = getPrefProto(featureFlag, preferences),
                            default = remoteFeatureFlag?.value ?: featureFlag.isEnabledDefault
                        )
                    }
            }
        } ?: dataStore.data
            .catch { exception -> handleExceptions(exception) }
            .map { preferences -> fromBooleanPrefProto(getPrefProto(featureFlag, preferences)) }

    private fun getPrefProto(featureFlag: FeatureFlag, preferences: FeatureFlagsPreferences) = with(preferences) {
        when (featureFlag) {
            AUTOFILL_DEBUG_MODE -> autofillDebugModeEnabled
            EXTRA_LOGGING -> extraLoggingEnabled
            RENAME_ADMIN_TO_MANAGER -> renameAdminToManagerEnabled
            PASS_HIDE_SHOW_VAULT -> passHideShowVaultEnabled
            PASS_ALLOW_NO_VAULT -> passAllowNoVault
            PASS_USER_EVENTS_V1 -> passUserEventsV1Enabled
            PASS_GROUP_SHARE -> groupsEnabled
        }.value
    }
}
