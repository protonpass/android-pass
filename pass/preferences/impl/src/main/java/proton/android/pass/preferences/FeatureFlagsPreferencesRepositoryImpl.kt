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
import proton.android.pass.preferences.FeatureFlag.ADVANCED_ALIAS_MANAGEMENT_V1
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import proton.android.pass.preferences.FeatureFlag.CUSTOM_TYPE_V1
import proton.android.pass.preferences.FeatureFlag.DIGITAL_ASSET_LINKS
import proton.android.pass.preferences.FeatureFlag.EXTRA_LOGGING
import proton.android.pass.preferences.FeatureFlag.FILE_ATTACHMENTS_V1
import proton.android.pass.preferences.FeatureFlag.IN_APP_MESSAGES_V1
import proton.android.pass.preferences.FeatureFlag.ITEM_SHARING_V1
import proton.android.pass.preferences.FeatureFlag.NEW_LOGIN_FLOW
import proton.android.pass.preferences.FeatureFlag.SECURE_LINK_NEW_CRYPTO_V1
import proton.android.pass.preferences.FeatureFlag.SL_ALIASES_SYNC
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

        SL_ALIASES_SYNC -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { simpleLoginAliasesSyncEnabled.value }

        DIGITAL_ASSET_LINKS -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { digitalAssetLinkEnabled.value }

        ADVANCED_ALIAS_MANAGEMENT_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { advanceAliasManagementV1Enabled.value }

        ITEM_SHARING_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { itemSharingV1Enabled.value }

        IN_APP_MESSAGES_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { inAppMessagesV1Enabled.value }

        EXTRA_LOGGING -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { extraLoggingEnabled.value }

        FILE_ATTACHMENTS_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { fileAttachmentsV1Enabled.value }

        SECURE_LINK_NEW_CRYPTO_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { secureLinkNewCryptoV1Enabled.value }

        NEW_LOGIN_FLOW -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { newLoginFlowEnabled.value }

        CUSTOM_TYPE_V1 -> getFeatureFlag(
            key = featureFlag.key,
            defaultValue = featureFlag.isEnabledDefault
        ) { customTypeV1Enabled.value }
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> = when (featureFlag) {
        AUTOFILL_DEBUG_MODE -> setFeatureFlag {
            autofillDebugModeEnabled = boolFlagPrefProto(value)
        }

        SL_ALIASES_SYNC -> setFeatureFlag {
            simpleLoginAliasesSyncEnabled = boolFlagPrefProto(value)
        }

        DIGITAL_ASSET_LINKS -> setFeatureFlag {
            digitalAssetLinkEnabled = boolFlagPrefProto(value)
        }

        ADVANCED_ALIAS_MANAGEMENT_V1 -> setFeatureFlag {
            advanceAliasManagementV1Enabled = boolFlagPrefProto(value)
        }

        ITEM_SHARING_V1 -> setFeatureFlag {
            itemSharingV1Enabled = boolFlagPrefProto(value)
        }

        EXTRA_LOGGING -> setFeatureFlag {
            extraLoggingEnabled = boolFlagPrefProto(value)
        }

        IN_APP_MESSAGES_V1 -> setFeatureFlag {
            inAppMessagesV1Enabled = boolFlagPrefProto(value)
        }

        FILE_ATTACHMENTS_V1 -> setFeatureFlag {
            fileAttachmentsV1Enabled = boolFlagPrefProto(value)
        }

        SECURE_LINK_NEW_CRYPTO_V1 -> setFeatureFlag {
            secureLinkNewCryptoV1Enabled = boolFlagPrefProto(value)
        }

        NEW_LOGIN_FLOW -> setFeatureFlag {
            newLoginFlowEnabled = boolFlagPrefProto(value)
        }

        CUSTOM_TYPE_V1 -> setFeatureFlag {
            customTypeV1Enabled = boolFlagPrefProto(value)
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
            SL_ALIASES_SYNC -> simpleLoginAliasesSyncEnabled
            DIGITAL_ASSET_LINKS -> digitalAssetLinkEnabled
            ADVANCED_ALIAS_MANAGEMENT_V1 -> advanceAliasManagementV1Enabled
            ITEM_SHARING_V1 -> itemSharingV1Enabled
            IN_APP_MESSAGES_V1 -> inAppMessagesV1Enabled
            EXTRA_LOGGING -> extraLoggingEnabled
            FILE_ATTACHMENTS_V1 -> fileAttachmentsV1Enabled
            SECURE_LINK_NEW_CRYPTO_V1 -> secureLinkNewCryptoV1Enabled
            NEW_LOGIN_FLOW -> newLoginFlowEnabled
            CUSTOM_TYPE_V1 -> customTypeV1Enabled
        }.value
    }
}
