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

import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRejectInvite @Inject constructor() : RejectInvite {

    private var result: Result<Unit> = Result.success(Unit)
    private val memoryToken: MutableList<InviteToken> = mutableListOf()
    private val memoryId: MutableList<InviteId> = mutableListOf()

    fun getMemoryToken(): List<InviteToken> = memoryToken
    fun getMemoryId(): List<InviteId> = memoryId

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(inviteToken: InviteToken) {
        memoryToken.add(inviteToken)
        result.getOrThrow()
    }

    override suspend fun invoke(inviteId: InviteId) {
        memoryId.add(inviteId)
        result.getOrThrow()
    }

}

