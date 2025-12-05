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
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import javax.inject.Inject

class FakeUpdateVault @Inject constructor() : UpdateVault {

    private var result: Result<Share> = Result.failure(IllegalStateException("value not set"))

    private var value: Payload? = null

    data class Payload(
        val userId: SessionUserId?,
        val shareId: ShareId,
        val vault: NewVault
    )

    fun setResult(value: Result<Share>) {
        result = value
    }

    fun getSentValue(): Payload? = value

    override suspend fun invoke(
        userId: SessionUserId?,
        shareId: ShareId,
        vault: NewVault
    ): Share {
        value = Payload(userId, shareId, vault)
        return result.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }
}
