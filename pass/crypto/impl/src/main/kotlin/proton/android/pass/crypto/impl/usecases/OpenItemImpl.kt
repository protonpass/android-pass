/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.crypto.impl.usecases

import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.impl.Constants.ITEM_CFV
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.UserId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class OpenItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val itemContentSerializer: ItemContentSerializer
) : OpenItem {

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput = encryptionContextProvider.withEncryptionContext {
        open(
            response = response,
            share = share,
            shareKeys = shareKeys,
            encryptionContext = this@withEncryptionContext
        )
    }

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>,
        folderKey: FolderKey?,
        encryptionContext: EncryptionContext
    ): OpenItemOutput = when (share.shareType) {
        ShareType.Vault -> openItemWithVaultShare(
            share = share,
            response = response,
            shareKeys = shareKeys,
            folderKey = folderKey,
            encryptionContext = encryptionContext
        )

        ShareType.Item -> openItemWithItemShare(
            response = response,
            shareId = share.id,
            userId = share.userId,
            shareKeys = shareKeys,
            encryptionContext = encryptionContext
        )
    }

    private fun openItemWithVaultShare(
        share: Share,
        shareKeys: List<ShareKey>,
        folderKey: FolderKey?,
        response: EncryptedItemRevision,
        encryptionContext: EncryptionContext
    ): OpenItemOutput {
        val shareId = share.id
        val itemKey = response.key ?: throw IllegalStateException(
            "ItemRevision should contain a key for Vault share " +
                "[share=${shareId.id}] [itemId=${response.itemId}]"
        )
        val decodedItemKey = Base64.decodeBase64(itemKey)

        val decryptedItemKey = if (response.folderId != null && folderKey != null) {
            val decryptedFolderKey = EncryptionKey(encryptionContext.decrypt(folderKey.key))
            encryptionContextProvider.withEncryptionContext(decryptedFolderKey) {
                EncryptionKey(decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey))
            }
        } else {
            val shareKey = shareKeys.firstOrNull { it.rotation == response.keyRotation }
                ?: throw KeyNotFound(
                    "Could not find ShareKey " +
                        "[share=${shareId.id}] [keyRotation=${response.keyRotation}]"
                )
            val decryptedShareKey = EncryptionKey(encryptionContext.decrypt(shareKey.key))
            encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
                EncryptionKey(decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey))
            }
        }

        val encryptedItemKey = encryptionContext.encrypt(decryptedItemKey.value())

        val decodedItemContents = Base64.decodeBase64(response.content)
        val decryptedContents = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(decodedItemContents), EncryptionTag.ItemContent)
        }

        if (response.contentFormatVersion > ITEM_CFV) {
            PassLogger.w(TAG, "Unknown Item ContentFormatVersion: ${response.contentFormatVersion}")
        }

        return OpenItemOutput(
            item = createDomainObject(
                response = response,
                shareId = shareId,
                decryptedContents = decryptedContents,
                encryptionContext = encryptionContext,
                userId = share.userId
            ),
            itemKey = encryptedItemKey
        )
    }

    private fun openItemWithItemShare(
        response: EncryptedItemRevision,
        shareId: ShareId,
        shareKeys: List<ShareKey>,
        encryptionContext: EncryptionContext,
        userId: UserId
    ): OpenItemOutput {

        val shareKey = shareKeys.firstOrNull { it.rotation == response.keyRotation }
            ?: throw KeyNotFound(
                "Could not find ShareKey " +
                    "[share=${shareId.id}] [keyRotation=${response.keyRotation}]"
            )

        val decryptedShareKey = EncryptionKey(encryptionContext.decrypt(shareKey.key))

        val decodedItemContents = Base64.decodeBase64(response.content)
        val decryptedContents = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(decodedItemContents), EncryptionTag.ItemContent)
        }

        return OpenItemOutput(
            item = createDomainObject(
                response = response,
                shareId = shareId,
                decryptedContents = decryptedContents,
                encryptionContext = encryptionContext,
                userId = userId
            ),
            itemKey = null
        )
    }

    private fun createDomainObject(
        response: EncryptedItemRevision,
        shareId: ShareId,
        userId: UserId,
        decryptedContents: ByteArray,
        encryptionContext: EncryptionContext
    ): Item = with(encryptionContext) {
        val parsed = itemContentSerializer.deserializeItem(
            bytes = decryptedContents,
            encryptionContext = this,
            aliasEmail = response.aliasEmail
        )
        Item(
            id = ItemId(response.itemId),
            userId = userId,
            itemUuid = parsed.itemUuid,
            revision = response.revision,
            shareId = shareId,
            folderId = response.folderId?.let(::FolderId),
            title = encrypt(parsed.title),
            note = encrypt(parsed.note),
            content = encrypt(decryptedContents),
            itemType = parsed.itemType,
            packageInfoSet = parsed.packageInfoSet,
            state = response.state,
            createTime = Instant.fromEpochSeconds(response.createTime),
            modificationTime = Instant.fromEpochSeconds(response.modifyTime),
            lastAutofillTime = response.lastUseTime.toOption().map(Instant::fromEpochSeconds),
            isPinned = response.isPinned,
            pinTime = response.pinTime.toOption().map(Instant::fromEpochSeconds),
            itemFlags = ItemFlags(response.flags),
            shareCount = response.shareCount,
            contentFormatVersion = response.contentFormatVersion,
            shareType = if (response.key != null) ShareType.Vault else ShareType.Item
        )
    }

    private companion object {

        private const val TAG = "OpenItemImpl"

    }

}
