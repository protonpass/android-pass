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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.proton.android.pass.preferences.BoolFlagPrefProto
import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class FeatureFlagsPreferencesRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val featureFlagManager: FeatureFlagManager,
    private val dataStore: DataStore<FeatureFlagsPreferences>
) : FeatureFlagsPreferencesRepository {

    override fun <T> get(featureFlag: FeatureFlag): Flow<T> =
        when (featureFlag) {
            AUTOFILL_DEBUG_MODE -> getFeatureFlag(featureFlag.key) { autofillDebugModeEnabled.value }
        }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> =
        when (featureFlag) {
            AUTOFILL_DEBUG_MODE -> setFeatureFlag {
                autofillDebugModeEnabled = boolFlagPrefProto(value)
            }
        }

    private fun <T> getFeatureFlag(key: String?, prefGetter: FeatureFlagsPreferences.() -> BooleanPrefProto): Flow<T> =
        if (key != null) {
            accountManager.getPrimaryUserId()
                .filterNotNull()
                .flatMapLatest { userId ->
                    featureFlagManager.observe(userId = userId, featureId = FeatureId(key))
                }
                .flatMapLatest { ff ->
                    dataStore.data
                        .catch { exception -> handleExceptions(exception) }
                        .map { preferences ->
                            fromBooleanPrefProto(
                                prefGetter(preferences),
                                ff?.value ?: false
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

    private fun <T> boolFlagPrefProto(
        value: T?
    ): BoolFlagPrefProto {
        val builder = BoolFlagPrefProto.newBuilder()
        value?.let { builder.value = (it as Boolean).toBooleanPrefProto() }
        return builder.build()
    }

    private suspend fun FlowCollector<FeatureFlagsPreferences>.handleExceptions(
        exception: Throwable
    ) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            emit(FeatureFlagsPreferences.getDefaultInstance())
        } else {
            throw exception
        }
    }
}
