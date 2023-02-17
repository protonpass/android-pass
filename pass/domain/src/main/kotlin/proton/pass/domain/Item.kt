package proton.pass.domain

import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.Option

@JvmInline
value class ItemId(val id: String)

data class Item(
    val id: ItemId,
    val revision: Long,
    val shareId: ShareId,
    val itemType: ItemType,
    val title: EncryptedString,
    val note: EncryptedString,
    val content: EncryptedByteArray,
    val allowedPackageNames: List<String>,
    val createTime: Instant,
    val modificationTime: Instant,
    val lastAutofillTime: Option<Instant>
)
