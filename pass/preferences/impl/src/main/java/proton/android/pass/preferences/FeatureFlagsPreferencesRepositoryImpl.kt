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
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag.CUSTOM_FIELDS_ENABLED
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
            CUSTOM_FIELDS_ENABLED -> {
                val key = featureFlag.key
                if (key != null) {
                    accountManager.getPrimaryUserId()
                        .filterNotNull()
                        .flatMapLatest { userId ->
                            featureFlagManager.observe(userId = userId, featureId = FeatureId(key))
                                .flatMapLatest { ff ->
                                    dataStore.data
                                        .catch { exception -> handleExceptions(exception) }
                                        .map { preferences ->
                                            fromBooleanPrefProto(
                                                preferences.customFieldsEnabled.value,
                                                ff?.value ?: false
                                            ) as T
                                        }
                                }
                        }
                } else {
                    dataStore.data
                        .catch { exception -> handleExceptions(exception) }
                        .map { fromBooleanPrefProto(it.customFieldsEnabled.value) as T }
                }
            }
        }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> =
        when (featureFlag) {
            CUSTOM_FIELDS_ENABLED -> runCatching {
                runBlocking {
                    dataStore.updateData { prefs ->
                        prefs.toBuilder()
                            .setCustomFieldsEnabled(boolFlagPrefProto(value))
                            .build()
                    }
                }
                return@runCatching
            }
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
