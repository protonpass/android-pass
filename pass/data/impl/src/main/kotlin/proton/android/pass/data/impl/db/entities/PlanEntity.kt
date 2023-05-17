package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = PlanEntity.TABLE,
    primaryKeys = [PlanEntity.Columns.USER_ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [PlanEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlanEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.TYPE, defaultValue = "free")
    val type: String,
    @ColumnInfo(name = Columns.INTERNAL_NAME, defaultValue = "")
    val internalName: String,
    @ColumnInfo(name = Columns.DISPLAY_NAME, defaultValue = "")
    val displayName: String,
    @ColumnInfo(name = Columns.VAULT_LIMIT)
    val vaultLimit: Int,
    @ColumnInfo(name = Columns.ALIAS_LIMIT)
    val aliasLimit: Int,
    @ColumnInfo(name = Columns.TOTP_LIMIT)
    val totpLimit: Int,
    @ColumnInfo(name = Columns.UPDATED_AT, defaultValue = "0")
    val updatedAt: Long,
    @ColumnInfo(name = Columns.HIDE_UPGRADE, defaultValue = "0")
    val hideUpgrade: Boolean,
    @ColumnInfo(name = Columns.TRIAL_END, defaultValue = "null")
    val trialEnd: Long?
) {
    object Columns {
        const val USER_ID = "user_id"
        const val TYPE = "type"
        const val INTERNAL_NAME = "internal_name"
        const val DISPLAY_NAME = "display_name"
        const val VAULT_LIMIT = "vault_limit"
        const val ALIAS_LIMIT = "alias_limit"
        const val TOTP_LIMIT = "totp_limit"
        const val UPDATED_AT = "updated_at"
        const val HIDE_UPGRADE = "hide_upgrade"
        const val TRIAL_END = "trial_end"
    }

    companion object {
        const val TABLE = "PlanEntity"
    }
}
