package proton.pass.domain

import java.util.Date
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey

data class ShareId(val id: String)
data class VaultId(val id: String)

data class Share(
    val id: ShareId,
    val shareType: ShareType,
    val targetId: String,
    val permission: SharePermission,
    val vaultId: VaultId,
    val signingKey: SigningKey,
    val content: EncryptedByteArray?, // Can be null if targetType is Item
    val nameKeyId: String?,
    val expirationTime: Date?,
    val createTime: Date,

    override val keys: List<VaultKey>
) : KeyHolder
