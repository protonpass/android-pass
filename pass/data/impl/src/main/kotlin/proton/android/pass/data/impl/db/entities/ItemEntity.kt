package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = ItemEntity.TABLE,
    primaryKeys = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [ItemEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ItemEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [ItemEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.REVISION)
    val revision: Long,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int,
    @ColumnInfo(name = Columns.KEY_ROTATION)
    val keyRotation: String,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String,
    @ColumnInfo(name = Columns.KEY)
    val key: String?,
    @ColumnInfo(name = Columns.STATE)
    val state: Int,
    @ColumnInfo(name = Columns.ITEM_TYPE)
    val itemType: Int,
    @ColumnInfo(name = Columns.ALIAS_EMAIL)
    val aliasEmail: String?,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,
    @ColumnInfo(name = Columns.MODIFY_TIME)
    val modifyTime: Long,
    @ColumnInfo(name = Columns.LAST_USED_TIME, defaultValue = "0")
    val lastUsedTime: Long,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_TITLE)
    val encryptedTitle: EncryptedString,
    @ColumnInfo(name = Columns.ENCRYPTED_NOTE)
    val encryptedNote: EncryptedString,
    @ColumnInfo(name = Columns.ENCRYPTED_CONTENT)
    val encryptedContent: EncryptedByteArray,
    @ColumnInfo(name = Columns.ENCRYPTED_KEY)
    val encryptedKey: EncryptedByteArray?,
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_ID = "share_id"
        const val REVISION = "revision"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val KEY_ROTATION = "key_rotation"
        const val CONTENT = "content"
        const val KEY = "key"
        const val STATE = "state"
        const val ITEM_TYPE = "item_type"
        const val ALIAS_EMAIL = "alias_email"
        const val CREATE_TIME = "create_time"
        const val MODIFY_TIME = "modify_time"
        const val LAST_USED_TIME = "last_used_time"
        const val ENCRYPTED_TITLE = "encrypted_title"
        const val ENCRYPTED_CONTENT = "encrypted_content"
        const val ENCRYPTED_NOTE = "encrypted_note"
        const val ENCRYPTED_KEY = "encrypted_key"
    }

    companion object {
        const val TABLE = "ItemEntity"
    }
}
