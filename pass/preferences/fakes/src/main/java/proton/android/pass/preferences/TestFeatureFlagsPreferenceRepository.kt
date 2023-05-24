package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestFeatureFlagsPreferenceRepository @Inject constructor() :
    FeatureFlagsPreferencesRepository {
    override fun <T> get(featureFlag: FeatureFlag): Flow<T> = when (featureFlag) {
        FeatureFlag.CUSTOM_FIELDS_ENABLED -> flowOf(true as T)
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> =
        Result.success(Unit)
}
