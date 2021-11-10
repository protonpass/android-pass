package me.proton.core.pass.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.serialization.Serializable
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = SecretEntity.TABLE,
    primaryKeys = [SecretEntity.Columns.ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [SecretEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [SecretEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SecretEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.NAME)
    val name: String,
    @ColumnInfo(name = Columns.TYPE)
    val type: Int,
    @ColumnInfo(name = Columns.IS_UPLOADED, defaultValue = "false")
    val isUploaded: Boolean,
    @ColumnInfo(name = Columns.CONTENTS)
    val contents: String,
    @ColumnInfo(name = Columns.ASSOCIATED_URIS)
    val associatedUris: String,
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val NAME = "name"
        const val TYPE = "type"
        const val IS_UPLOADED = "is_uploaded"
        const val CONTENTS = "contents"
        const val ASSOCIATED_URIS = "associated_uris"
    }

    companion object {
        const val TABLE = "secrets"
    }
}

@Serializable
data class LoginSecretContents(
    val identity: String,
    val password: String
)
