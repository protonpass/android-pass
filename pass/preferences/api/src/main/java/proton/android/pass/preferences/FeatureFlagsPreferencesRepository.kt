package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow

interface FeatureFlagsPreferencesRepository {
    operator fun <T> get(featureFlag: FeatureFlag): Flow<T>
    fun <T> set(
        featureFlag: FeatureFlag,
        value: T? = null
    ): Result<Unit>
}
