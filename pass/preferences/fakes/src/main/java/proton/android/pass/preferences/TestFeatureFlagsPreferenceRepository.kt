package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestFeatureFlagsPreferenceRepository @Inject constructor() :
    FeatureFlagsPreferencesRepository {
    override fun <T> get(featureFlags: FeatureFlags): Flow<T> = when (featureFlags) {
        FeatureFlags.IAP_ENABLED -> flowOf(true as T)
    }

    override fun <T> set(featureFlags: FeatureFlags, value: T?, override: T?): Result<Unit> =
        Result.success(Unit)
}
