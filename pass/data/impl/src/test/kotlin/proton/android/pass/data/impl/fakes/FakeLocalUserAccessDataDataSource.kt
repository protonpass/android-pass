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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.UserAccessDataEntity
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource

class FakeLocalUserAccessDataDataSource : LocalUserAccessDataDataSource {

    private val observeFlow: MutableStateFlow<UserAccessDataEntity?> = MutableStateFlow(null)

    override suspend fun store(entity: UserAccessDataEntity) {
        observeFlow.emit(entity)
    }

    override fun observe(userId: UserId): Flow<UserAccessDataEntity?> = observeFlow

    override suspend fun updateProtonMonitorState(userId: UserId, enabled: Boolean) {
    }

    override suspend fun updateAliasMonitorState(userId: UserId, enabled: Boolean) {
    }
}
