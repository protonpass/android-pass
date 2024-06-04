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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.errors.WrongExtraPasswordException
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.data.impl.requests.SetupExtraPasswordRequest
import proton.android.pass.data.impl.responses.ExtraPasswordGetSrpData
import javax.inject.Inject

interface RemoteExtraPasswordDataSource {
    suspend fun setupExtraPassword(userId: UserId, request: SetupExtraPasswordRequest)
    suspend fun removeExtraPassword(userId: UserId)
    suspend fun getExtraPasswordAuthData(userId: UserId): ExtraPasswordGetSrpData
    suspend fun sendExtraPasswordAuthData(userId: UserId, request: ExtraPasswordSendSrpDataRequest)
}

class RemoteExtraPasswordDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteExtraPasswordDataSource {
    override suspend fun setupExtraPassword(userId: UserId, request: SetupExtraPasswordRequest) {
        apiProvider.get<PasswordManagerApi>(userId).invoke {
            setupExtraPassword(request)
        }.valueOrThrow
    }

    override suspend fun removeExtraPassword(userId: UserId) {
        apiProvider.get<PasswordManagerApi>(userId).invoke {
            removeExtraPassword()
        }.valueOrThrow
    }

    override suspend fun getExtraPasswordAuthData(userId: UserId) = apiProvider.get<PasswordManagerApi>(userId).invoke {
        getSrpInfo()
    }.valueOrThrow.data

    override suspend fun sendExtraPasswordAuthData(userId: UserId, request: ExtraPasswordSendSrpDataRequest) {
        val res = apiProvider.get<PasswordManagerApi>(userId).invoke {
            sendSrpInfo(request)
        }

        when (res) {
            is ApiResult.Error.Http -> when (res.proton?.code) {
                WRONG_PASSWORD -> throw WrongExtraPasswordException()
                TOO_MANY_WRONG_ATTEMPTS -> throw TooManyExtraPasswordAttemptsException()
                else -> res.valueOrThrow
            }
            else -> res.valueOrThrow
        }
    }

    companion object {
        private const val WRONG_PASSWORD = 2011
        private const val TOO_MANY_WRONG_ATTEMPTS = 2026
    }

}
