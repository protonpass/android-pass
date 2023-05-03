package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = ShareKeyEntity.TABLE,
    primaryKeys = [ShareKeyEntity.Columns.ROTATION, ShareKeyEntity.Columns.SHARE_ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [ShareKeyEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ShareKeyEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [ShareKeyEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShareKeyEntity(
    @ColumnInfo(name = Columns.ROTATION)
    val rotation: Long,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.KEY)
    val key: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,

    // Keystore Encrypted key
    @ColumnInfo(name = Columns.SYMMETRICALLY_ENCRYPTED_KEY)
    val symmetricallyEncryptedKey: EncryptedByteArray,
    @ColumnInfo(name = Columns.USER_KEY_ID)
    val userKeyId: String,
    @ColumnInfo(name = Columns.IS_ACTIVE, defaultValue = "1")
    val isActive: Boolean
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_ID = "share_id"
        const val ROTATION = "rotation"
        const val KEY = "key"
        const val CREATE_TIME = "create_time"
        const val SYMMETRICALLY_ENCRYPTED_KEY = "symmetrically_encrypted_key"
        const val USER_KEY_ID = "user_key_id"
        const val IS_ACTIVE = "is_active"
    }

    companion object {
        const val TABLE = "ShareKeyEntity"
    }
}
