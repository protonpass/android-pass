package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.SearchEntry
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface SearchEntryRepository {
    suspend fun store(userId: UserId, itemId: ItemId, shareId: ShareId)
    suspend fun deleteAll(userId: UserId)
    suspend fun deleteAllByShare(shareId: ShareId)
    suspend fun deleteEntry(shareId: ShareId, itemId: ItemId)
    fun observeAll(userId: UserId): Flow<List<SearchEntry>>
    fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntry>>
}
