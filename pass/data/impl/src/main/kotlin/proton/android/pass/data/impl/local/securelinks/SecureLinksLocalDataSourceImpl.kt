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

package proton.android.pass.data.impl.local.securelinks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.securelinks.SecureLinkEntity
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import javax.inject.Inject

class SecureLinksLocalDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : SecureLinksLocalDataSource {

    override suspend fun create(userId: UserId, secureLink: SecureLink) = database.secureLinksDao()
        .insertOrIgnore(secureLink.toEntity(userId))

    override fun observe(
        userId: UserId,
        secureLinkId: SecureLinkId
    ): Flow<SecureLink> = database.secureLinksDao()
        .observeSecureLink(userId = userId.id, id = secureLinkId.id)
        .map { entity -> entity.toDomain() }

    override fun observeAll(userId: UserId): Flow<List<SecureLink>> = database.secureLinksDao()
        .observeSecureLinks(userId = userId.id)
        .map { entities -> entities.map { entity -> entity.toDomain() } }

    private fun SecureLink.toEntity(userId: UserId) = SecureLinkEntity(
        userId = userId.id,
        id = id.id,
        shareId = shareId.id,
        itemId = itemId.id,
        expiration = expiration?.epochSeconds,
        maxViews = maxReadCount,
        views = readCount,
        url = url
    )

    private fun SecureLinkEntity.toDomain() = SecureLink(
        id = SecureLinkId(id = id),
        shareId = ShareId(id = shareId),
        itemId = ItemId(id = itemId),
        expiration = expiration?.let { epochSeconds -> Instant.fromEpochSeconds(epochSeconds) },
        maxReadCount = maxViews,
        readCount = views,
        url = url
    )

}
