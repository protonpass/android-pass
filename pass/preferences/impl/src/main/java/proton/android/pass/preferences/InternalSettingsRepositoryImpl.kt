package proton.android.pass.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject

class InternalSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<InternalSettings>
) : InternalSettingsRepository {
    override suspend fun setLastUnlockedTime(time: Instant): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setLastUnlockTime(time.epochSeconds)
                .build()
        }
        return@runCatching
    }


    override fun getLastUnlockedTime(): Flow<Option<Instant>> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings ->
            if (settings.lastUnlockTime == 0L) {
                None
            } else {
                val parsed = Instant.fromEpochSeconds(settings.lastUnlockTime)
                Some(parsed)
            }
        }

    override suspend fun clearSettings(): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .clear()
                .build()
        }
        return@runCatching
    }

    private suspend fun FlowCollector<InternalSettings>.handleExceptions(
        exception: Throwable
    ) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            emit(InternalSettings.getDefaultInstance())
        } else {
            throw exception
        }
    }

}
