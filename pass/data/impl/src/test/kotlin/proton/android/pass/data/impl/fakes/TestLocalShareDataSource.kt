package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.pass.domain.ShareId

class TestLocalShareDataSource : LocalShareDataSource {

    private var upsertResponse: Result<Unit> =
        Result.failure(IllegalStateException("upsertResponse not set"))
    private var disablePrimaryShareResponse: Result<Unit> =
        Result.failure(IllegalStateException("disablePrimaryShareResponse ot set"))
    private var getByIdResponse: Result<ShareEntity?> =
        Result.failure(IllegalStateException("getById not set"))
    private var deleteSharesResponse: Result<Boolean> =
        Result.failure(IllegalStateException("deleteShares not set"))
    private var hasSharesResponse: Result<Boolean> =
        Result.failure(IllegalStateException("hasShares not set"))
    private var setPrimaryShareStatusResult: Result<Unit> =
        Result.failure(IllegalStateException("primaryShareStatusResult not set"))
    private var deleteSharesForUserResult: Result<Unit> = Result.success(Unit)

    private val getAllSharesForUserFlow = testFlow<List<ShareEntity>>()
    private val getAllSharesForAddressFlow = testFlow<List<ShareEntity>>()

    private var deleteMemory: MutableList<Set<ShareId>> = mutableListOf()
    private var upsertMemory: MutableList<List<ShareEntity>> = mutableListOf()
    private var setPrimaryShareStatusMemory: MutableList<SetPrimarySharePayload> = mutableListOf()

    fun getDeleteMemory() = deleteMemory
    fun getUpsertMemory() = upsertMemory
    fun getSetPrimaryShareMemory() = setPrimaryShareStatusMemory

    fun setUpsertResponse(value: Result<Unit>) {
        upsertResponse = value
    }

    fun setDisablePrimaryShareResponse(value: Result<Unit>) {
        disablePrimaryShareResponse = value
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

    fun setSetPrimaryShareStatusResult(value: Result<Unit>) {
        setPrimaryShareStatusResult = value
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

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? =
        getByIdResponse.getOrThrow()

    override fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        getAllSharesForUserFlow

    override fun observeAllActiveSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        getAllSharesForUserFlow.map { shares -> shares.filter { it.isActive } }

    override fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>> =
        getAllSharesForAddressFlow

    override suspend fun deleteShares(shareIds: Set<ShareId>): Boolean {
        deleteMemory.add(shareIds)
        return deleteSharesResponse.getOrThrow()
    }

    override suspend fun hasShares(userId: UserId): Boolean = hasSharesResponse.getOrThrow()

    override suspend fun disablePrimaryShare(userId: UserId) {
        disablePrimaryShareResponse.getOrThrow()
    }

    override suspend fun setPrimaryShareStatus(
        userId: UserId,
        shareId: ShareId,
        isPrimary: Boolean
    ) {
        setPrimaryShareStatusMemory.add(SetPrimarySharePayload(userId, shareId, isPrimary))
        setPrimaryShareStatusResult.getOrThrow()
    }

    override suspend fun deleteSharesForUser(userId: UserId) {
        deleteSharesForUserResult.getOrThrow()
    }

    data class SetPrimarySharePayload(
        val userId: UserId,
        val shareId: ShareId,
        val isPrimary: Boolean
    )
}
