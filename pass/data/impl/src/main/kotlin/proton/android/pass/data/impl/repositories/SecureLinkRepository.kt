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

package proton.android.pass.data.impl.repositories

import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.usecases.publiclink.SecureLinkOptions
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSource
import proton.android.pass.data.impl.requests.CreateSecureLinkRequest
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

interface SecureLinkRepository {

    suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): String

}

class SecureLinkRepositoryImpl @Inject constructor(
    private val localItemDataSource: LocalItemDataSource,
    private val remoteSecureLinkDataSource: RemoteSecureLinkDataSource,
    private val encryptionContextProvider: EncryptionContextProvider
) : SecureLinkRepository {

    override suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): String {
        val item = localItemDataSource.getById(shareId, itemId) ?: throw IllegalStateException(
            "Item not found [shareId=${shareId.id}] [itemId=${itemId.id}]"
        )

        val key = item.encryptedKey ?: throw IllegalStateException(
            "Item does not have an itemKey [shareId=${shareId.id}] [itemId=${itemId.id}]"
        )

        val decryptedKey = encryptionContextProvider.withEncryptionContext { decrypt(key) }

        val linkKey = EncryptionKey.generate()
        val encryptedItemKey = encryptionContextProvider.withEncryptionContext(linkKey.clone()) {
            encrypt(decryptedKey, EncryptionTag.ItemKey)
        }
        val encodedEncryptedItemKey = Base64.encodeBase64String(encryptedItemKey.array)

        val request = CreateSecureLinkRequest(
            revision = item.revision,
            expirationTime = options.expirationTime.inWholeSeconds,
            maxReadCount = options.maxReadCount,
            encryptedItemKey = encodedEncryptedItemKey
        )

        val response = remoteSecureLinkDataSource.createSecureLink(
            userId = userId,
            shareId = shareId,
            itemId = itemId,
            request = request
        )

        val encodedLinkKey = Base64.encodeBase64String(linkKey.value(), Base64.Mode.UrlSafe)
        val concatenated = "${response.url}#$encodedLinkKey"

        return concatenated
    }

}
