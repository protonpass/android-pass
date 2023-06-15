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
import proton.android.pass.preferences.FeatureFlag.CREDIT_CARDS_ENABLED
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
            CUSTOM_FIELDS_ENABLED -> getFeatureFlag(featureFlag.key) { customFieldsEnabled.value }
            CREDIT_CARDS_ENABLED -> getFeatureFlag(featureFlag.key) { creditCardsEnabled.value }
        }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> =
        when (featureFlag) {
            CUSTOM_FIELDS_ENABLED -> setFeatureFlag {
                setCustomFieldsEnabled(boolFlagPrefProto(value))
            }
            CREDIT_CARDS_ENABLED -> setFeatureFlag {
                setCreditCardsEnabled(boolFlagPrefProto(value))
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
