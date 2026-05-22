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

package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.SearchResultItem
import proton.android.pass.data.api.usecases.GetItemsBySearchResult
import proton.android.pass.domain.Item
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemsBySearchResultImpl @Inject constructor(
    private val itemRepository: ItemRepository
) : GetItemsBySearchResult {

    override suspend fun invoke(userId: UserId, searchResults: List<SearchResultItem>): List<Item> {
        if (searchResults.isEmpty()) return emptyList()

        // Group by shareId for efficient batch fetching
        val groupedByShare = searchResults.groupBy { it.shareId }

        val items = mutableListOf<Item>()

        for ((shareId, results) in groupedByShare) {
            val itemIds = results.map { it.itemId }
            runCatching {
                val fetchedItems = itemRepository.getByIds(userId, shareId, itemIds)
                items.addAll(fetchedItems)
            }.onFailure {
                PassLogger.w(TAG, it, "Failed to fetch items for share: $shareId")
            }
        }

        // Maintain the original order from search results
        val itemMap = items.associateBy { it.shareId to it.id }
        return searchResults.mapNotNull { result ->
            itemMap[result.shareId to result.itemId]
        }
    }

    companion object {
        private const val TAG = "GetItemsBySearchResultImpl"
    }
}
