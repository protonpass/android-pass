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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class FakeRemoteItemKeyDataSource : RemoteItemKeyDataSource {

    var fetchLatestItemKeyCallCount: Int = 0
        private set

    private var response: ItemLatestKeyResponse? = null

    fun setFetchLatestItemKeyResponse(value: ItemLatestKeyResponse) {
        response = value
    }

    override suspend fun fetchLatestItemKey(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemLatestKeyResponse {
        fetchLatestItemKeyCallCount++
        return response ?: throw IllegalStateException("fetchLatestItemKey response not set")
    }
}
