package me.proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = PassEventEntity.TABLE,
    // All the 3 columns conform the Primary Key, as we want to update in case a new event comes
    // with the same combination of these 3 columns
    primaryKeys = [
        PassEventEntity.Columns.USER_ID,
        PassEventEntity.Columns.ADDRESS_ID,
        PassEventEntity.Columns.SHARE_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [PassEventEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [PassEventEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [PassEventEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PassEventEntity(
    @ColumnInfo(name = Columns.ID)
    val eventId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID)
    val addressId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.RETRIEVED_AT)
    val retrievedAt: Long
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_ID = "share_id"
        const val RETRIEVED_AT = "retrieved_at"
    }
    companion object {
        const val TABLE = "PassEventEntity"
    }
}
