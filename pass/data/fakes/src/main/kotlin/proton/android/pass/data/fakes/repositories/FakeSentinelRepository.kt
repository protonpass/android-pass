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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.core.repositories.SentinelRepository
import javax.inject.Inject

class FakeSentinelRepository @Inject constructor() : SentinelRepository {

    private val isSentinelEnabledFlow = MutableStateFlow(false)
    private val canEnableSentinelFlow = MutableStateFlow(false)

    fun setCanEnableSentinel(canEnable: Boolean) {
        canEnableSentinelFlow.tryEmit(canEnable)
    }

    override suspend fun disableSentinel() {
        isSentinelEnabledFlow.tryEmit(false)
    }

    override suspend fun enableSentinel() {
        isSentinelEnabledFlow.tryEmit(true)
    }

    override fun observeIsSentinelEnabled(): Flow<Boolean> = isSentinelEnabledFlow

    override fun observeCanEnableSentinel(userId: UserId): Flow<Boolean> = canEnableSentinelFlow

}
