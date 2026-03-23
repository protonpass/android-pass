/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.fakes.usecases.inappmessages

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeChangeInAppMessageStatus @Inject constructor() : ChangeInAppMessageStatus {

    data class Invocation(
        val userId: UserId,
        val messageId: InAppMessageId,
        val status: InAppMessageStatus
    )

    val invocations = mutableListOf<Invocation>()
    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        invocations += Invocation(userId, messageId, status)
        result.getOrThrow()
    }
}
