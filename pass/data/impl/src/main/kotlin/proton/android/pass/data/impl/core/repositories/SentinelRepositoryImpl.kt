/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.core.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.core.datasources.LocalSentinelDataSource
import proton.android.pass.data.api.core.datasources.RemoteSentinelDataSource
import proton.android.pass.data.api.core.repositories.SentinelRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

private const val TAG = "SentinelRepositoryImpl"

class SentinelRepositoryImpl @Inject constructor(
    private val remoteSentinelDataSource: RemoteSentinelDataSource,
    private val localSentinelDataSource: LocalSentinelDataSource
) : SentinelRepository {

    override suspend fun disableSentinel() {
        safeRunCatching { remoteSentinelDataSource.disableSentinel() }
            .onSuccess {
                localSentinelDataSource.disableSentinel()
            }
    }

    override suspend fun enableSentinel() {
        safeRunCatching { remoteSentinelDataSource.enableSentinel() }
            .onSuccess {
                localSentinelDataSource.enableSentinel()
            }
    }

    override fun observeIsSentinelEnabled(): Flow<Boolean> = localSentinelDataSource
        .observeIsSentinelEnabled()
        .onStart {
            emit(localSentinelDataSource.observeIsSentinelEnabled().first())

            runCatching { remoteSentinelDataSource.isSentinelEnabled() }
                .onFailure { error ->
                    PassLogger.w(TAG, "Could not fetch sentinel status")
                    PassLogger.w(TAG, error)
                }
                .onSuccess { isSentinelEnabled ->
                    if (isSentinelEnabled) {
                        localSentinelDataSource.enableSentinel()
                    } else {
                        localSentinelDataSource.disableSentinel()
                    }
                }
        }
        .distinctUntilChanged()

    override fun observeCanEnableSentinel(userId: UserId): Flow<Boolean> =
        localSentinelDataSource.observeCanEnableSentinel(userId)
            .map { localValue ->
                when (localValue) {
                    is Some -> localValue.value
                    is None -> {
                        val canEnable = remoteSentinelDataSource.canEnableSentinel()
                        localSentinelDataSource.updateCanEnableSentinel(userId, canEnable)
                        canEnable
                    }
                }
            }

}
