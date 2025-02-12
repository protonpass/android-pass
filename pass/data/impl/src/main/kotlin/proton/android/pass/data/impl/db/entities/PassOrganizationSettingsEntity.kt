/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = PassOrganizationSettingsEntity.TABLE,
    primaryKeys = [
        PassOrganizationSettingsEntity.Columns.USER_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [PassOrganizationSettingsEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PassOrganizationSettingsEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,

    @ColumnInfo(name = Columns.CAN_UPDATE)
    val canUpdate: Boolean,

    @ColumnInfo(name = Columns.SHARE_MODE)
    val shareMode: Int,

    @ColumnInfo(name = Columns.HAS_ORGANIZATION)
    val hasOrganization: Boolean,

    @ColumnInfo(name = Columns.FORCE_LOCK_SECONDS, defaultValue = "0")
    val forceLockSeconds: Int,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_ALLOWED)
    val randomPasswordAllowed: Boolean?,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_MIN_LENGTH)
    val randomPasswordMinLength: Int?,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_MAX_LENGTH)
    val randomPasswordMaxLength: Int?,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_INCLUDE_NUMBERS)
    val randomPasswordIncludeNumbers: Boolean?,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_INCLUDE_SYMBOLS)
    val randomPasswordIncludeSymbols: Boolean?,

    @ColumnInfo(name = Columns.RANDOM_PASSWORD_INCLUDE_UPPERCASE)
    val randomPasswordIncludeUppercase: Boolean?,

    @ColumnInfo(name = Columns.MEMORABLE_PASSWORD_ALLOWED)
    val memorablePasswordAllowed: Boolean?,

    @ColumnInfo(name = Columns.MEMORABLE_PASSWORD_MIN_WORDS)
    val memorablePasswordMinWords: Int?,

    @ColumnInfo(name = Columns.MEMORABLE_PASSWORD_MAX_WORDS)
    val memorablePasswordMaxWords: Int?,

    @ColumnInfo(name = Columns.MEMORABLE_PASSWORD_CAPITALIZED)
    val memorablePasswordCapitalize: Boolean?,

    @ColumnInfo(name = Columns.MEMORABLE_PASSWORD_INCLUDE_NUMBERS)
    val memorablePasswordIncludeNumbers: Boolean?,

    @ColumnInfo(name = Columns.VAULT_CREATE_MODE)
    val vaultCreateMode: Int?,

    @ColumnInfo(name = Columns.ITEM_SHARE_MODE)
    val itemShareMode: Int?,

    @ColumnInfo(name = Columns.SECURE_LINKS_MODE)
    val secureLinksMode: Int?
) {
    object Columns {
        const val USER_ID = "user_id"
        const val CAN_UPDATE = "can_update"
        const val SHARE_MODE = "share_mode"
        const val FORCE_LOCK_SECONDS = "force_lock_seconds"
        const val HAS_ORGANIZATION = "has_organization"
        const val RANDOM_PASSWORD_ALLOWED = "random_password_allowed"
        const val RANDOM_PASSWORD_MIN_LENGTH = "random_password_min_length"
        const val RANDOM_PASSWORD_MAX_LENGTH = "random_password_max_length"
        const val RANDOM_PASSWORD_INCLUDE_NUMBERS = "random_password_include_numbers"
        const val RANDOM_PASSWORD_INCLUDE_SYMBOLS = "random_password_include_symbols"
        const val RANDOM_PASSWORD_INCLUDE_UPPERCASE = "random_password_include_uppercase"
        const val MEMORABLE_PASSWORD_ALLOWED = "memorable_password_allowed"
        const val MEMORABLE_PASSWORD_MIN_WORDS = "memorable_password_min_words"
        const val MEMORABLE_PASSWORD_MAX_WORDS = "memorable_password_max_words"
        const val MEMORABLE_PASSWORD_INCLUDE_NUMBERS = "memorable_password_include_numbers"
        const val MEMORABLE_PASSWORD_CAPITALIZED = "memorable_password_capitalized"
        const val VAULT_CREATE_MODE = "vault_create_mode"
        const val ITEM_SHARE_MODE = "item_share_mode"
        const val SECURE_LINKS_MODE = "secure_links_mode"
    }

    companion object {
        const val TABLE = "PassOrganizationSettingsEntity"

        fun empty(userId: String) = PassOrganizationSettingsEntity(
            userId = userId,
            canUpdate = false,
            shareMode = 0,
            hasOrganization = false,
            forceLockSeconds = 0,
            randomPasswordAllowed = null,
            randomPasswordMinLength = null,
            randomPasswordMaxLength = null,
            randomPasswordIncludeNumbers = null,
            randomPasswordIncludeSymbols = null,
            randomPasswordIncludeUppercase = null,
            memorablePasswordAllowed = null,
            memorablePasswordMinWords = null,
            memorablePasswordMaxWords = null,
            memorablePasswordCapitalize = null,
            memorablePasswordIncludeNumbers = null,
            vaultCreateMode = null,
            itemShareMode = null,
            secureLinksMode = null
        )
    }
}
