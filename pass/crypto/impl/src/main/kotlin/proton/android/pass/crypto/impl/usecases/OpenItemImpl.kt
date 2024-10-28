/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.domain.entity.UserId
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
import proton.android.pass.crypto.impl.Constants.ITEM_CONTENT_FORMAT_VERSION
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.domain.Flags
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class OpenItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItem {

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput = encryptionContextProvider.withEncryptionContext {
        open(response, share, shareKeys, this@withEncryptionContext)
    }

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>,
        encryptionContext: EncryptionContext
    ): OpenItemOutput {
        return when (share.shareType) {
            ShareType.Vault -> openItemWithVaultShare(
                response = response,
                shareId = share.id,
                userId = share.userId,
                shareKeys = shareKeys,
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
    }

    private fun openItemWithVaultShare(
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

        val itemKey = response.key
            ?: throw IllegalStateException(
                "ItemRevision should contain a key for Vault share " +
                    "[share=${shareId.id}] [itemId=${response.itemId}]"
            )
        val decodedItemKey = Base64.decodeBase64(itemKey)

        val decryptedShareKey = EncryptionKey(encryptionContext.decrypt(shareKey.key))

        val decryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            EncryptionKey(decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey))
        }

        val encryptedItemKey = encryptionContext.encrypt(decryptedItemKey.value())

        val decodedItemContents = Base64.decodeBase64(response.content)
        val decryptedContents = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(decodedItemContents), EncryptionTag.ItemContent)
        }

        if (response.contentFormatVersion > ITEM_CONTENT_FORMAT_VERSION) {
            PassLogger.w(TAG, "Unknown Item ContentFormatVersion: ${response.contentFormatVersion}")
        }

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return OpenItemOutput(
            item = createDomainObject(
                response = response,
                shareId = shareId,
                decoded = decoded,
                decryptedContents = decryptedContents,
                encryptionContext = encryptionContext,
                userId = userId
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

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return OpenItemOutput(
            item = createDomainObject(
                response = response,
                shareId = shareId,
                decoded = decoded,
                decryptedContents = decryptedContents,
                encryptionContext = encryptionContext,
                userId = userId
            ),
            itemKey = null
        )
    }

    @Suppress("LongParameterList")
    private fun createDomainObject(
        response: EncryptedItemRevision,
        shareId: ShareId,
        userId: UserId,
        decoded: ItemV1.Item,
        decryptedContents: ByteArray,
        encryptionContext: EncryptionContext
    ): Item = with(encryptionContext) {
        Item(
            id = ItemId(response.itemId),
            userId = userId,
            itemUuid = decoded.metadata.itemUuid,
            revision = response.revision,
            shareId = shareId,
            title = encrypt(decoded.metadata.name),
            note = encrypt(decoded.metadata.note),
            content = encrypt(decryptedContents),
            itemType = ItemType.fromParsed(this, decoded, aliasEmail = response.aliasEmail),
            packageInfoSet = decoded.platformSpecific.android.allowedAppsList
                .map { PackageInfo(PackageName(it.packageName), AppName(it.appName)) }
                .toSet(),
            state = response.state,
            createTime = Instant.fromEpochSeconds(response.createTime),
            modificationTime = Instant.fromEpochSeconds(response.modifyTime),
            lastAutofillTime = response.lastUseTime.toOption().map(Instant::fromEpochSeconds),
            isPinned = response.isPinned,
            flags = Flags(response.flags)
        )
    }


    companion object {
        private const val TAG = "OpenItemImpl"
    }
}

