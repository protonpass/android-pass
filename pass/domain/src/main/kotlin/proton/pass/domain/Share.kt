package proton.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.common.api.Option
import java.util.Date

@JvmInline
value class ShareId(val id: String)

@JvmInline
value class VaultId(val id: String)

data class Share(
    val id: ShareId,
    val shareType: ShareType,
    val targetId: String,
    val permission: SharePermission,
    val isPrimary: Boolean,
    val vaultId: VaultId,
    val content: Option<EncryptedByteArray>, // Can be None if targetType is Item
    val expirationTime: Date?,
    val createTime: Date,
    val color: ShareColor,
    val icon: ShareIcon
)
