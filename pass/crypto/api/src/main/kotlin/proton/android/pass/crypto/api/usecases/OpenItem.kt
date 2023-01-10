package proton.android.pass.crypto.api.usecases

import me.proton.core.key.domain.entity.key.PublicKey
import proton.pass.domain.Item
import proton.pass.domain.Share
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey

data class EncryptedItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val rotationId: String,
    val content: String,
    val userSignature: String,
    val itemKeySignature: String,
    val state: Int,
    val signatureEmail: String,
    val aliasEmail: String?,
    val labels: List<String>,
    val createTime: Long,
    val modifyTime: Long,
    val lastUseTime: Long,
    val revisionTime: Long
)

interface OpenItem {
    fun open(
        response: EncryptedItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item
}
