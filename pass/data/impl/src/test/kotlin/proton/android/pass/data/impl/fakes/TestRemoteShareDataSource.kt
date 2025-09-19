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
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.ShareId

class TestRemoteShareDataSource : RemoteShareDataSource {

    private var createVaultResponse: Result<ShareResponse> =
        Result.failure(IllegalStateException("createVaultResponse not set"))
    private var updateVaultResponse: Result<ShareResponse> =
        Result.failure(IllegalStateException("updateVaultResponse not set"))
    private var deleteVaultResponse: Result<Unit> =
        Result.failure(IllegalStateException("deleteVaultResponse not set"))
    private var getSharesResponse: Result<List<ShareResponse>> =
        Result.failure(IllegalStateException("getSharesResponse not set"))
    private var getShareByIdResponse: Result<ShareResponse> =
        Result.failure(IllegalStateException("getShareByIdResponse not set"))
    private var markAsPrimaryResponse: Result<Unit> =
        Result.failure(IllegalStateException("markAsPrimaryResponse not set"))
    private var leaveVaultResponse: Result<Unit> = Result.success(Unit)
    private var changeShareVisibilityResponse: Result<List<ShareResponse>> =
        Result.failure(IllegalStateException("changeShareVisibilityResponse not set"))

    fun setCreateVaultResponse(value: Result<ShareResponse>) {
        createVaultResponse = value
    }

    fun setUpdateVaultResponse(value: Result<ShareResponse>) {
        updateVaultResponse = value
    }

    fun setDeleteVaultResponse(value: Result<Unit>) {
        deleteVaultResponse = value
    }

    fun setGetSharesResponse(value: Result<List<ShareResponse>>) {
        getSharesResponse = value
    }

    fun setGetShareByIdResponse(value: Result<ShareResponse>) {
        getShareByIdResponse = value
    }

    fun setMarkAsPrimaryResponse(value: Result<Unit>) {
        markAsPrimaryResponse = value
    }

    fun setLeaveVaultResponse(value: Result<Unit>) {
        leaveVaultResponse = value
    }

    override suspend fun createVault(userId: UserId, body: CreateVaultRequest): ShareResponse =
        createVaultResponse.getOrThrow()

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        body: UpdateVaultRequest
    ): ShareResponse = updateVaultResponse.getOrThrow()

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) = deleteVaultResponse.getOrThrow()

    override suspend fun retrieveShares(userId: UserId): List<ShareResponse> = getSharesResponse.getOrThrow()

    override suspend fun retrieveShareById(userId: UserId, shareId: ShareId): ShareResponse =
        getShareByIdResponse.getOrThrow()

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId) {
        markAsPrimaryResponse.getOrThrow()
    }

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {
        leaveVaultResponse.getOrThrow()
    }

    override suspend fun batchChangeShareVisibility(
        userId: UserId,
        shareVisibilityChanges: Map<ShareId, Boolean>
    ): List<ShareResponse> = changeShareVisibilityResponse.getOrThrow()

}
