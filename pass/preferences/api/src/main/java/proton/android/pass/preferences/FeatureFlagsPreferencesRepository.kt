package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow

interface FeatureFlagsPreferencesRepository {
    operator fun <T> get(featureFlags: FeatureFlags): Flow<T>
    fun <T> set(
        featureFlags: FeatureFlags,
        value: T? = null,
        override: T? = null
    ): Result<Unit>
}
