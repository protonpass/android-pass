package proton.android.pass.crypto.api.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.pass.domain.Item
import proton.pass.domain.Share
import proton.pass.domain.key.ShareKey

data class EncryptedItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val keyRotation: Long,
    val content: String,
    val state: Int,
    val key: String?,
    val aliasEmail: String?,
    val createTime: Long,
    val modifyTime: Long,
    val lastUseTime: Long?,
    val revisionTime: Long
)

data class OpenItemOutput(
    val item: Item,
    val itemKey: EncryptedByteArray?
)

interface OpenItem {
    fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput
}
