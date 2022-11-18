package me.proton.android.pass.data.impl.db.entities

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
    @ColumnInfo(name = Columns.INVITER_EMAIL)
    val inviterEmail: String,
    @ColumnInfo(name = Columns.ACCEPTANCE_SIGNATURE)
    val acceptanceSignature: String,
    @ColumnInfo(name = Columns.INVITER_ACCEPTANCE_SIGNATURE)
    val inviterAcceptanceSignature: String,
    @ColumnInfo(name = Columns.SIGNING_KEY)
    val signingKey: String,
    @ColumnInfo(name = Columns.SIGNING_KEY_PASSPHRASE)
    val signingKeyPassphrase: String?,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String?,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int?,
    @ColumnInfo(name = Columns.CONTENT_ENCRYPTED_ADDRESS_SIGNATURE)
    val contentEncryptedAddressSignature: String?,
    @ColumnInfo(name = Columns.CONTENT_ENCRYPTED_VAULT_SIGNATURE)
    val contentEncryptedVaultSignature: String?,
    @ColumnInfo(name = Columns.CONTENT_SIGNATURE_EMAIL)
    val contentSignatureEmail: String?,
    @ColumnInfo(name = Columns.KEYSTORE_ENCRYPTED_CONTENT)
    val keystoreEncryptedContent: EncryptedByteArray?,
    @ColumnInfo(name = Columns.KEYSTORE_ENCRYPTED_PASSPHRASE)
    val keystoreEncryptedPassphrase: EncryptedByteArray?,
    @ColumnInfo(name = Columns.NAME_KEY_ID)
    val nameKeyId: String?,
    @ColumnInfo(name = Columns.EXPIRATION_TIME)
    val expirationTime: Long?,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_TYPE = "share_type"
        const val TARGET_ID = "target_id"
        const val INVITER_EMAIL = "inviter_email"
        const val ACCEPTANCE_SIGNATURE = "acceptance_signature"
        const val INVITER_ACCEPTANCE_SIGNATURE = "inviter_acceptance_signature"
        const val PERMISSION = "permission"
        const val VAULT_ID = "vault_id"
        const val SIGNING_KEY = "signing_key"
        const val SIGNING_KEY_PASSPHRASE = "signing_key_passphrase"
        const val CONTENT = "content"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val CONTENT_ENCRYPTED_ADDRESS_SIGNATURE = "content_encrypted_address_signature"
        const val CONTENT_ENCRYPTED_VAULT_SIGNATURE = "content_encrypted_vault_signature"
        const val CONTENT_SIGNATURE_EMAIL = "content_signature_email"
        const val KEYSTORE_ENCRYPTED_CONTENT = "keystore_encrypted_content"
        const val KEYSTORE_ENCRYPTED_PASSPHRASE = "keystore_encrypted_passphrase"
        const val NAME_KEY_ID = "name_key_id"
        const val EXPIRATION_TIME = "expiration_time"
        const val CREATE_TIME = "create_time"
    }

    companion object {
        const val TABLE = "ShareEntity"
    }
}
