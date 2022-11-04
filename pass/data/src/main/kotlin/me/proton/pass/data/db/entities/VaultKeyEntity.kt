package me.proton.pass.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = VaultKeyEntity.TABLE,
    primaryKeys = [VaultKeyEntity.Columns.ID, VaultKeyEntity.Columns.SHARE_ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [VaultKeyEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [VaultKeyEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [VaultKeyEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VaultKeyEntity(
    @ColumnInfo(name = Columns.ID)
    val rotationId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.ROTATION)
    val rotation: Long,
    @ColumnInfo(name = Columns.KEY)
    val key: String,
    @ColumnInfo(name = Columns.KEY_PASSPHRASE)
    val keyPassphrase: String?,
    @ColumnInfo(name = Columns.KEY_SIGNATURE)
    val keySignature: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_KEY_PASSPHRASE)
    val encryptedKeyPassphrase: EncryptedByteArray?
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_ID = "share_id"
        const val ROTATION = "rotation"
        const val KEY = "key"
        const val KEY_PASSPHRASE = "key_passphrase"
        const val KEY_SIGNATURE = "key_signature"
        const val CREATE_TIME = "create_time"
        const val ENCRYPTED_KEY_PASSPHRASE = "encrypted_passphrase"
    }

    companion object {
        const val TABLE = "VaultKeyEntity"
    }
}
