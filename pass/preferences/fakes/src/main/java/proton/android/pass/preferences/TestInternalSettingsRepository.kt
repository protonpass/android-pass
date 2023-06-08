package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestInternalSettingsRepository @Inject constructor() : InternalSettingsRepository {

    private val lastUnlockedTimeFlow = MutableStateFlow<Option<Instant>>(None)

    override suspend fun setLastUnlockedTime(time: Instant): Result<Unit> {
        lastUnlockedTimeFlow.update { Some(time) }
        return Result.success(Unit)
    }

    override fun getLastUnlockedTime(): Flow<Option<Instant>> = lastUnlockedTimeFlow

    override suspend fun clearSettings(): Result<Unit> = Result.success(Unit)
}
