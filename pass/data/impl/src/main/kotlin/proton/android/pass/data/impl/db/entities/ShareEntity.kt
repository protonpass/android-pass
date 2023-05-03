package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = ShareEntity.TABLE,
    primaryKeys = [ShareEntity.Columns.ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [ShareEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ShareEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShareEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.VAULT_ID)
    val vaultId: String,
    @ColumnInfo(name = Columns.SHARE_TYPE)
    val targetType: Int,
    @ColumnInfo(name = Columns.TARGET_ID)
    val targetId: String,
    @ColumnInfo(name = Columns.PERMISSION)
    val permission: Int,
    @ColumnInfo(name = Columns.IS_PRIMARY, defaultValue = "0")
    val isPrimary: Boolean,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String?,
    @ColumnInfo(name = Columns.CONTENT_KEY_ROTATION)
    val contentKeyRotation: Long?,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int?,
    @ColumnInfo(name = Columns.EXPIRATION_TIME)
    val expirationTime: Long?,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_CONTENT)
    val encryptedContent: EncryptedByteArray?,

    @ColumnInfo(name = Columns.IS_ACTIVE, defaultValue = "1")
    val isActive: Boolean
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_TYPE = "share_type"
        const val TARGET_ID = "target_id"
        const val PERMISSION = "permission"
        const val IS_PRIMARY = "is_primary"
        const val VAULT_ID = "vault_id"
        const val CONTENT = "content"
        const val CONTENT_KEY_ROTATION = "content_key_rotation"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val EXPIRATION_TIME = "expiration_time"
        const val CREATE_TIME = "create_time"
        const val ENCRYPTED_CONTENT = "encrypted_content"
        const val IS_ACTIVE = "is_active"
    }

    companion object {
        const val TABLE = "ShareEntity"
    }
}
