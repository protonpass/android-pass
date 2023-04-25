package proton.android.pass.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.proton.android.pass.preferences.BoolFlagPrefProto
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlags.IAP_ENABLED
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class FeatureFlagsPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<FeatureFlagsPreferences>
) : FeatureFlagsPreferencesRepository {

    override fun <T> get(featureFlags: FeatureFlags): Flow<T> =
        when (featureFlags) {
            IAP_ENABLED ->
                dataStore.data
                    .catch { exception -> handleExceptions(exception) }
                    .map { it.iapEnabled.getBooleanPref() as T }
        }

    override fun <T> set(featureFlags: FeatureFlags, value: T?, override: T?): Result<Unit> =
        when (featureFlags) {
            IAP_ENABLED -> runCatching {
                runBlocking {
                    dataStore.updateData { prefs ->
                        prefs.toBuilder()
                            .setIapEnabled(boolFlagPrefProto(override, value))
                            .build()
                    }
                }
                return@runCatching
            }
        }

    private fun <T> boolFlagPrefProto(
        override: T?,
        value: T?
    ): BoolFlagPrefProto {
        val builder = BoolFlagPrefProto.newBuilder()
        override?.let { builder.overridden = (it as Boolean).toBooleanPrefProto() }
        value?.let { builder.value = (it as Boolean).toBooleanPrefProto() }
        return builder.build()
    }

    private fun BoolFlagPrefProto.getBooleanPref(default: Boolean = false): Boolean =
        fromBooleanPrefProto(overridden, fromBooleanPrefProto(value, default))

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
