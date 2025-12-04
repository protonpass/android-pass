/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger

private const val TAG = "PaginatedFetcher"

suspend fun <T, R> fetchAllPaginated(
    fetchPage: suspend (lastToken: String?) -> PaginatedResponse<T>,
    mapToDomain: (T) -> R,
    storeResults: suspend (List<R>) -> Unit
) {
    coroutineScope {
        val allItems = mutableListOf<R>()
        var lastToken: String? = null
        var totalFetched = 0

        safeRunCatching {
            do {
                ensureActive()
                val response = fetchPage(lastToken)
                val newItems = response.items.map(mapToDomain)
                allItems.addAll(newItems)
                totalFetched += newItems.size

                lastToken = if (totalFetched < response.total && response.items.isNotEmpty()) {
                    response.lastId
                } else {
                    null
                }
            } while (lastToken != null)

            storeResults(allItems)
        }.onFailure {
            PassLogger.w(TAG, "Failed to fetch paginated data")
            PassLogger.w(TAG, it)
        }
    }
}

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val lastId: String?
)

