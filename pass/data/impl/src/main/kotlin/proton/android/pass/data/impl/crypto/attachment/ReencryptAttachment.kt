/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.data.impl.crypto.attachment

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import javax.inject.Inject

@JvmInline
value class ReencryptedMetadata(val value: EncryptedByteArray)

@JvmInline
value class ReencryptedKey(val value: EncryptedByteArray)

data class AttachmentToReencrypt(
    val encryptedMetadata: EncryptedString,
    val encryptedKey: EncryptedString,
    val encryptionVersion: Int
)

interface ReencryptAttachment {
    suspend operator fun invoke(
        encryptedItemKey: EncryptedByteArray,
        attachments: List<AttachmentToReencrypt>
    ): Pair<List<ReencryptedMetadata>, List<ReencryptedKey>>
}

class ReencryptAttachmentImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ReencryptAttachment {

    override suspend fun invoke(
        encryptedItemKey: EncryptedByteArray,
        attachments: List<AttachmentToReencrypt>
    ): Pair<List<ReencryptedMetadata>, List<ReencryptedKey>> {
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey))
        }
        val decryptedKeys = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            attachments.map { attachment ->
                val decrypted = decrypt(
                    EncryptedByteArray(Base64.decodeBase64(attachment.encryptedKey)),
                    EncryptionTag.FileKey
                )
                EncryptionKey(decrypted)
            }
        }

        val decryptedMetadata = decryptedKeys.zip(attachments).map { (key, attachment) ->
            encryptionContextProvider.withEncryptionContextSuspendable(key.clone()) {
                val content = EncryptedByteArray(Base64.decodeBase64(attachment.encryptedMetadata))
                val encryptionVersion = attachment.encryptionVersion
                when (encryptionVersion) {
                    1 -> decrypt(
                        content = content,
                        tag = EncryptionTag.FileData
                    )
                    2 -> decrypt(
                        content = content,
                        tag = EncryptionTag.FileMetadata
                    )
                    else -> throw IllegalStateException("Unknown encryptionVersion $encryptionVersion")
                }
            }
        }
        return encryptionContextProvider.withEncryptionContextSuspendable {
            decryptedMetadata.map { ReencryptedMetadata(encrypt(it)) } to
                decryptedKeys.map { ReencryptedKey(encrypt(it.value())) }
        }
    }
}
