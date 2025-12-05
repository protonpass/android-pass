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

import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.data.impl.crypto.NewUserInviteSignatureManager
import proton.android.pass.domain.key.InviteKey

class FakeCreateNewUserInviteSignature : NewUserInviteSignatureManager {

    private var createResult: Result<String> = Result.success("")

    private var verifyResult: Result<Unit> = Result.success(Unit)

    var hasCreateBeenInvoked: Boolean = false
        private set

    private var hasVerifyBeenInvoked: Boolean = false

    fun setCreateResult(value: Result<String>) {
        createResult = value
    }

    fun setVerifyResult(value: Result<Unit>) {
        verifyResult = value
    }

    override fun create(
        inviterUserAddress: UserAddress,
        email: String,
        inviteKey: InviteKey
    ): Result<String> {
        hasCreateBeenInvoked = true
        return createResult
    }

    override fun validate(
        inviterUserAddress: UserAddress,
        signature: String,
        email: String,
        inviteKey: InviteKey
    ): Result<Unit> {
        hasVerifyBeenInvoked = true
        return verifyResult
    }
}
