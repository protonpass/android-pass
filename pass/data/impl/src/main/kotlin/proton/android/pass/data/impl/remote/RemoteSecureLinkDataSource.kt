/*
 * Copyright (c) 2024 Proton AG
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
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CreateSecureLinkRequest
import proton.android.pass.data.impl.responses.CreatedSecureLink
import proton.android.pass.data.impl.responses.GetSecureLinkResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLinkId
import javax.inject.Inject

interface RemoteSecureLinkDataSource {

    suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: CreateSecureLinkRequest
    ): CreatedSecureLink

    suspend fun getAllSecureLinks(userId: UserId): List<GetSecureLinkResponse>

    suspend fun deleteSecureLink(userId: UserId, secureLinkId: SecureLinkId)

}

class RemoteSecureLinkDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteSecureLinkDataSource {

    override suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: CreateSecureLinkRequest
    ): CreatedSecureLink = apiProvider.get<PasswordManagerApi>(userId)
        .invoke { generateSecureLink(shareId = shareId.id, itemId = itemId.id, request = request) }
        .valueOrThrow
        .secureLink

    override suspend fun getAllSecureLinks(userId: UserId): List<GetSecureLinkResponse> =
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getAllSecureLinks() }
            .valueOrThrow
            .links

    override suspend fun deleteSecureLink(userId: UserId, secureLinkId: SecureLinkId) {
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke { deleteSecureLink(secureLinkId = secureLinkId.id) }
            .valueOrThrow
    }

}
