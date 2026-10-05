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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.data.impl.requests.SetupExtraPasswordRequest
import proton.android.pass.data.impl.responses.ExtraPasswordGetSrpData

class FakeRemoteExtraPasswordDataSource : RemoteExtraPasswordDataSource {

    private var authData: ExtraPasswordGetSrpData? = null
    private var sendAuthDataResult: Result<Unit> = Result.success(Unit)

    private val mutableAuthDataRequests = mutableListOf<UserId>()
    val authDataRequests: List<UserId> = mutableAuthDataRequests

    private val mutableSentAuthData = mutableListOf<Pair<UserId, ExtraPasswordSendSrpDataRequest>>()
    val sentAuthData: List<Pair<UserId, ExtraPasswordSendSrpDataRequest>> = mutableSentAuthData

    fun setAuthData(value: ExtraPasswordGetSrpData) {
        authData = value
    }

    fun setSendAuthDataResult(value: Result<Unit>) {
        sendAuthDataResult = value
    }

    override suspend fun getExtraPasswordAuthData(userId: UserId): ExtraPasswordGetSrpData {
        mutableAuthDataRequests.add(userId)
        return requireNotNull(authData) { "Auth data not set" }
    }

    override suspend fun sendExtraPasswordAuthData(userId: UserId, request: ExtraPasswordSendSrpDataRequest) {
        mutableSentAuthData.add(userId to request)
        sendAuthDataResult.getOrThrow()
    }

    override suspend fun setupExtraPassword(userId: UserId, request: SetupExtraPasswordRequest) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun removeExtraPassword(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }
}
