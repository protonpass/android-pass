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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.local.ItemWithTotp
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId

class TestLocalItemDataSource : LocalItemDataSource {

    private val memory: MutableList<ItemEntity> = mutableListOf()
    private var summary: MutableStateFlow<ItemCountSummary> =
        MutableStateFlow(ItemCountSummary.Initial)
    private var itemCount: MutableStateFlow<Map<ShareId, ShareItemCount>> =
        MutableStateFlow(emptyMap())
    private val itemsWithTotpFlow = testFlow<Result<List<ItemWithTotp>>>()
    private val itemEntityFlow = testFlow<ItemEntity>()

    private val sharedItemsFlow = testFlow<List<ItemEntity>>()

    fun getMemory(): List<ItemEntity> = memory

    fun emitSummary(value: ItemCountSummary) {
        summary.tryEmit(value)
    }

    fun emitItemCount(value: Map<ShareId, ShareItemCount>) {
        itemCount.tryEmit(value)
    }

    suspend fun emitItemEntity(newItemEntity: ItemEntity) {
        itemEntityFlow.emit(newItemEntity)
    }

    fun emitSharedItems(value: List<ItemEntity>) {
        sharedItemsFlow.tryEmit(value)
    }

    fun emitItemsWithTotp(value: Result<List<ItemWithTotp>>) {
        itemsWithTotpFlow.tryEmit(value)
    }

    override suspend fun upsertItem(item: ItemEntity) {
        memory.add(item)
    }

    override suspend fun upsertItems(items: List<ItemEntity>) {
        memory.addAll(items)
    }

    override fun observeItems(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>
    ): Flow<List<ItemEntity>> = flowOf(memory)

    override fun observePinnedItems(
        userId: UserId,
        shareIds: List<ShareId>,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> {
        throw IllegalStateException("Not yet implemented")
    }

    override fun observeItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemEntity?> = itemEntityFlow

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemEntity {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getByIdList(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun setItemStates(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>,
        itemState: ItemState
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getTrashedItems(userId: UserId, shareIds: List<ShareId>): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Boolean {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateLastUsedTime(
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): ItemEntity {
        throw IllegalStateException("Not yet implemented")
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> = itemCount

    override suspend fun getItemsPendingForTotpMigration(): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override fun observeItemsWithTotp(userId: UserId, shareIds: List<ShareId>): Flow<List<ItemWithTotp>> =
        itemsWithTotpFlow.map { it.getOrThrow() }

    override fun countAllItemsWithTotp(userId: UserId, shareIds: List<ShareId>): Flow<Int> =
        itemsWithTotpFlow.map { it.getOrThrow().count() }

    override fun observeItemsWithPasskeys(userId: UserId, shareIds: List<ShareId>): Flow<List<ItemEntity>> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItemFlags(
        shareId: ShareId,
        itemId: ItemId,
        flags: Int
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override fun getByVaultIdAndItemId(
        userIds: List<UserId>,
        vaultId: VaultId,
        itemId: ItemId
    ): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override fun findUserId(shareId: ShareId, itemId: ItemId): Option<UserId> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItemsPendingForPasskeyMigration(): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        onlyShared: Boolean,
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ): Flow<ItemCountSummary> = summary

}
