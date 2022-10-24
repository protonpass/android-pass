package me.proton.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.ShareSelection
import me.proton.pass.domain.entity.NewAlias
import me.proton.pass.domain.entity.PackageName

interface ItemRepository {
    suspend fun createItem(userId: UserId, share: Share, contents: ItemContents): Result<Item>
    suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Result<Item>
    suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Result<Item>

    fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState
    ): Flow<Result<List<Item>>>

    suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Item>
    suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun untrashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun clearTrash(userId: UserId): Result<Unit>
    suspend fun addPackageToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: PackageName
    ): Result<Item>

    suspend fun refreshItems(
        userId: UserId,
        share: Share
    ): Result<List<Item>>
}
