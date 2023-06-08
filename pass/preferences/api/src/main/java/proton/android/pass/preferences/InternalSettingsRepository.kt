package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option

interface InternalSettingsRepository {

    suspend fun setLastUnlockedTime(time: Instant): Result<Unit>
    fun getLastUnlockedTime(): Flow<Option<Instant>>

    suspend fun clearSettings(): Result<Unit>
}
