package me.proton.core.pass.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.BLOB
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.user.data.entity.AddressEntity

@Entity(
    tableName = SecretEntity.TABLE,
    primaryKeys = [SecretEntity.Columns.ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [SecretEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SecretEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.NAME)
    val name: String,
    @ColumnInfo(name = Columns.CONTENTS, typeAffinity = BLOB)
    val contents: ByteArray,
    @ColumnInfo(name = Columns.ASSOCIATED_URIS)
    val associatedUris: String,
) {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is SecretEntity -> {
                id == other.id &&
                        addressId == other.addressId &&
                        name == other.name &&
                        contents.contentEquals(other.contents) &&
                        associatedUris == other.associatedUris
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + addressId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + associatedUris.hashCode()
        return result
    }

    object Columns {
        const val ID = "id"
        const val ADDRESS_ID = "address_id"
        const val NAME = "name"
        const val CONTENTS = "contents"
        const val ASSOCIATED_URIS = "associated_uris"
    }

    companion object {
        const val TABLE = "secrets"
    }
}