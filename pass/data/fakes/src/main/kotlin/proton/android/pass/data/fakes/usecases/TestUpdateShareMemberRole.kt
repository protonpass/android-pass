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

import proton.android.pass.data.api.usecases.shares.UpdateShareMemberRole
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestUpdateShareMemberRole @Inject constructor() : UpdateShareMemberRole {
    private var result: Result<Unit> = Result.success(Unit)
    private val memory: MutableList<Payload> = mutableListOf()

    fun getMemory(): List<Payload> = memory

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole
    ) {
        memory.add(Payload(shareId, memberShareId, memberShareRole))
        result.getOrThrow()
    }

    data class Payload(
        val shareId: ShareId,
        val memberShareId: ShareId,
        val role: ShareRole
    )
}
