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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.BatchHideUnhideShareRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareDataSource {

    override suspend fun createVault(userId: UserId, body: CreateVaultRequest): ShareResponse {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createVault(body) }
        when (res) {
            is ApiResult.Success -> return res.value.share
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    if (res.proton?.code == CODE_CANNOT_CREATE_MORE_VAULTS) {
                        throw CannotCreateMoreVaultsError()
                    }
                }
                throw res.cause ?: Exception("Create vault failed")
            }
        }
    }

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        body: UpdateVaultRequest
    ): ShareResponse = api.get<PasswordManagerApi>(userId)
        .invoke { updateVault(shareId.id, body).share }
        .valueOrThrow

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) {
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteVault(shareId.id) }
            .valueOrThrow
    }

    override suspend fun retrieveShares(userId: UserId): List<ShareResponse> = api.get<PasswordManagerApi>(userId)
        .invoke { getShares().shares }
        .valueOrThrow

    override suspend fun retrieveShareById(userId: UserId, shareId: ShareId): ShareResponse? =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                val res = getShare(shareId.id)
                if (res.code == PROTON_RESPONSE_OK) {
                    res.share
                } else {
                    null
                }
            }
            .valueOrThrow

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId) = api.get<PasswordManagerApi>(userId)
        .invoke { markAsPrimary(shareId.id) }
        .valueOrThrow

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {
        api.get<PasswordManagerApi>(userId)
            .invoke { leaveShare(shareId.id) }
            .valueOrThrow
    }

    override suspend fun batchChangeShareVisibility(
        userId: UserId,
        shareVisibilityChanges: Map<ShareId, Boolean>
    ): List<ShareResponse> = api.get<PasswordManagerApi>(userId)
        .invoke {
            val (toShow, toHide) = shareVisibilityChanges.entries.partition { it.value }
            val request = BatchHideUnhideShareRequest(
                toHide.map { it.key.id },
                toShow.map { it.key.id }
            )
            changeShareVisibility(request).list
        }
        .valueOrThrow

    private companion object {

        private const val PROTON_RESPONSE_OK = 1_000

        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300_007

    }

}
