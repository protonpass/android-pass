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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

class TestLocalShareDataSource : LocalShareDataSource {

    private var upsertResponse: Result<Unit> =
        Result.failure(IllegalStateException("upsertResponse not set"))
    private var getByIdResponse: Result<ShareEntity?> =
        Result.failure(IllegalStateException("getById not set"))
    private var deleteSharesResponse: Result<Boolean> =
        Result.failure(IllegalStateException("deleteShares not set"))
    private var hasSharesResponse: Result<Boolean> =
        Result.failure(IllegalStateException("hasShares not set"))
    private var deleteSharesForUserResult: Result<Unit> = Result.success(Unit)
    private var updateOwnershipStatusResult: Result<Unit> = Result.success(Unit)

    private val getAllSharesForUserFlow = testFlow<List<ShareEntity>>()
    private val getAllSharesForAddressFlow = testFlow<List<ShareEntity>>()
    private val getShareCountFlow = testFlow<Result<Int>>()
    private val observeByIdFlow = testFlow<Result<ShareEntity?>>()

    private val observeSharesByTypeFlow = testFlow<Result<List<ShareEntity>>>()

    private val observeSharedWithMeIds = testFlow<Result<List<String>>>()

    private val observeSharedByMeIds = testFlow<Result<List<String>>>()

    private var deleteMemory: MutableList<Set<ShareId>> = mutableListOf()
    private var upsertMemory: MutableList<List<ShareEntity>> = mutableListOf()

    fun getDeleteMemory() = deleteMemory
    fun getUpsertMemory() = upsertMemory

    fun setUpsertResponse(value: Result<Unit>) {
        upsertResponse = value
    }

    fun setGetByIdResponse(value: Result<ShareEntity?>) {
        getByIdResponse = value
    }

    fun setDeleteSharesResponse(value: Result<Boolean>) {
        deleteSharesResponse = value
    }

    fun setHasSharesResponse(value: Result<Boolean>) {
        hasSharesResponse = value
    }

    fun emitAllSharesForUser(value: List<ShareEntity>) {
        getAllSharesForUserFlow.tryEmit(value)
    }

    fun emitAllSharesForAddress(value: List<ShareEntity>) {
        getAllSharesForAddressFlow.tryEmit(value)
    }

    fun setDeleteSharesForUserResult(value: Result<Unit>) {
        deleteSharesForUserResult = value
    }

    override suspend fun upsertShares(shares: List<ShareEntity>) {
        upsertMemory.add(shares)
        upsertResponse.getOrThrow()
    }

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? = getByIdResponse.getOrThrow()

    override fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>> = getAllSharesForUserFlow

    override fun observeAllActiveSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        getAllSharesForUserFlow.map { shares -> shares.filter { it.isActive } }

    override fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>> = getAllSharesForAddressFlow

    override suspend fun deleteShares(shareIds: Set<ShareId>): Boolean {
        deleteMemory.add(shareIds)
        return deleteSharesResponse.getOrThrow()
    }

    override suspend fun hasShares(userId: UserId): Boolean = hasSharesResponse.getOrThrow()

    override suspend fun deleteSharesForUser(userId: UserId) {
        deleteSharesForUserResult.getOrThrow()
    }

    override fun observeActiveVaultCount(userId: UserId): Flow<Int> = getShareCountFlow
        .map { it.getOrThrow() }

    override suspend fun updateOwnershipStatus(
        userId: UserId,
        shareId: ShareId,
        isOwner: Boolean
    ) {
        updateOwnershipStatusResult.getOrThrow()
    }

    override fun observeById(userId: UserId, shareId: ShareId): Flow<ShareEntity?> =
        observeByIdFlow.map { it.getOrThrow() }

    override fun observeByType(
        userId: UserId,
        shareType: ShareType,
        isActive: Boolean?
    ): Flow<List<ShareEntity>> = observeSharesByTypeFlow.map { it.getOrThrow() }

    override fun observeSharedWithMeIds(userId: UserId): Flow<List<String>> = observeSharedWithMeIds
        .map { it.getOrThrow() }

    override fun observeSharedByMeIds(userId: UserId): Flow<List<String>> = observeSharedByMeIds
        .map { it.getOrThrow() }

}
