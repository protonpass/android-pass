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

package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.domain.Share
import proton.android.pass.domain.entity.NewVault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCreateVault @Inject constructor() : CreateVault {

    private var result: Result<Share> =
        Result.failure(IllegalStateException("FakeCreateVault result not set"))

    private val memory = mutableListOf<Payload>()

    fun memory(): List<Payload> = memory

    fun setResult(result: Result<Share>) {
        this.result = result
    }

    override suspend fun invoke(userId: SessionUserId?, vault: NewVault): Share {
        memory.add(Payload(vault))
        return result.getOrThrow()
    }

    data class Payload(val vault: NewVault)
}
