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

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.data.impl.responses.UserAccessResponse

class TestRemotePlanDataSource : RemotePlanDataSource {

    private var result: Result<UserAccessResponse> =
        Result.failure(IllegalStateException("TestRemotePlanDataSource not initialized"))

    fun setResult(value: Result<UserAccessResponse>) {
        result = value
    }

    override suspend fun sendUserAccessAndGetPlan(userId: UserId): UserAccessResponse =
        result.getOrThrow()
}
