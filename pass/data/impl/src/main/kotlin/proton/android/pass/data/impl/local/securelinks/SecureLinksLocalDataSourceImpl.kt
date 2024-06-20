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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.securelinks.SecureLinkEntity
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import javax.inject.Inject

class SecureLinksLocalDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val encryptionContextProvider: EncryptionContextProvider
) : SecureLinksLocalDataSource {

    override suspend fun create(userId: UserId, secureLink: SecureLink) =
        encryptionContextProvider.withEncryptionContext {
            secureLink.toEntity(userId, this@withEncryptionContext)
        }.let { entity ->
            database.secureLinksDao().insertOrIgnore(entity)
        }

    override suspend fun delete(userId: UserId, secureLinkId: SecureLinkId) = read(userId, secureLinkId)
        .let { secureLink ->
            encryptionContextProvider.withEncryptionContext {
                secureLink.toEntity(userId, this@withEncryptionContext)
            }
        }
        .let { entity ->
            database.secureLinksDao().delete(entity)
        }

    override suspend fun getAll(userId: UserId): List<SecureLink> = observeAll(userId).first()

    override suspend fun getCount(userId: UserId): Int = observeCount(userId).first()

    override fun observe(userId: UserId, secureLinkId: SecureLinkId): Flow<SecureLink> = database.secureLinksDao()
        .observeSecureLink(userId = userId.id, linkId = secureLinkId.id)
        .map { entity ->
            encryptionContextProvider.withEncryptionContext {
                entity.toDomain(this@withEncryptionContext)
            }
        }

    override fun observeAll(userId: UserId): Flow<List<SecureLink>> = database.secureLinksDao()
        .observeSecureLinks(userId = userId.id)
        .map { entities ->
            encryptionContextProvider.withEncryptionContext {
                entities.map { entity ->
                    entity.toDomain(this@withEncryptionContext)
                }
            }
        }

    override fun observeCount(userId: UserId): Flow<Int> = database.secureLinksDao()
        .observeSecureLinksCount(userId = userId.id)

    override suspend fun read(userId: UserId, secureLinkId: SecureLinkId): SecureLink = observe(userId, secureLinkId)
        .first()

    override suspend fun update(userId: UserId, secureLinks: List<SecureLink>) =
        encryptionContextProvider.withEncryptionContext {
            secureLinks.map { secureLink ->
                secureLink.toEntity(userId, this@withEncryptionContext)
            }
        }.let { entities -> database.secureLinksDao().insertOrUpdate(*entities.toTypedArray()) }

    private fun SecureLink.toEntity(userId: UserId, encryptionContext: EncryptionContext) = SecureLinkEntity(
        userId = userId.id,
        linkId = id.id,
        shareId = shareId.id,
        itemId = itemId.id,
        expirationInSeconds = expirationInSeconds,
        isExpired = isExpired,
        maxViews = maxReadCount,
        views = readCount,
        url = encryptionContext.encrypt(url)
    )

    private fun SecureLinkEntity.toDomain(encryptionContext: EncryptionContext) = SecureLink(
        id = SecureLinkId(id = linkId),
        shareId = ShareId(id = shareId),
        itemId = ItemId(id = itemId),
        expirationInSeconds = expirationInSeconds,
        isExpired = isExpired,
        maxReadCount = maxViews,
        readCount = views,
        url = encryptionContext.decrypt(url)
    )

}
