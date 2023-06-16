/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
