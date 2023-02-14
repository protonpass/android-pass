package proton.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import java.util.Date

data class ShareId(val id: String)
data class VaultId(val id: String)

data class Share(
    val id: ShareId,
    val shareType: ShareType,
    val targetId: String,
    val permission: SharePermission,
    val vaultId: VaultId,
    val content: EncryptedByteArray?, // Can be null if targetType is Item
    val expirationTime: Date?,
    val createTime: Date,
)
