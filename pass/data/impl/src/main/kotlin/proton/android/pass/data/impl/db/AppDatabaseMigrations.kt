/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.db

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.db.UserKeyDatabase
import me.proton.core.userrecovery.data.db.DeviceRecoveryDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.db.entities.UserAccessDataEntity
import proton.android.pass.data.impl.db.entities.securelinks.SecureLinkEntity
import proton.android.pass.domain.items.ItemCategory

@Suppress("ClassNaming")
object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ObservabilityDatabase.MIGRATION_0.migrate(database)
        }
    }

    @DeleteTable.Entries(value = [DeleteTable(tableName = "SelectedShareEntity")])
    class MIGRATION_2_3 : AutoMigrationSpec

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            OrganizationDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("UPDATE AccountMetadataEntity SET product = 'Pass'")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AddressDatabase.MIGRATION_4.migrate(database)
            PublicAddressDatabase.MIGRATION_2.migrate(database)
            KeyTransparencyDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                    ALTER TABLE ${ShareEntity.TABLE}
                    ADD COLUMN ${ShareEntity.Columns.IS_ACTIVE} INTEGER NOT NULL DEFAULT 1
                """.trimIndent()
            )
            database.execSQL(
                """
                    ALTER TABLE ${ShareKeyEntity.TABLE}
                    ADD COLUMN ${ShareKeyEntity.Columns.USER_KEY_ID} TEXT NOT NULL DEFAULT ''
                """.trimIndent()
            )
            database.execSQL(
                """
                    ALTER TABLE ${ShareKeyEntity.TABLE}
                    ADD COLUMN ${ShareKeyEntity.Columns.IS_ACTIVE} INTEGER NOT NULL DEFAULT 1
                """.trimIndent()
            )
            database.execSQL(
                """
                    UPDATE ${ShareKeyEntity.TABLE}
                    SET ${ShareKeyEntity.Columns.USER_KEY_ID} = (
                        SELECT keyId
                        FROM UserKeyEntity
                        WHERE UserKeyEntity.userId = ${ShareKeyEntity.TABLE}.${ShareKeyEntity.Columns.USER_ID}
                          AND UserKeyEntity.isPrimary = 1
                          LIMIT 1
                    )
                """.trimIndent()
            )
        }
    }

    @RenameTable.Entries(
        value = [RenameTable(fromTableName = "PlanLimitsEntity", toTableName = "PlanEntity")]
    )
    class MIGRATION_11_12 : AutoMigrationSpec

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.addTableColumn(
                table = ItemEntity.TABLE,
                column = ItemEntity.Columns.HAS_TOTP,
                type = "INTEGER",
                defaultValue = null
            )
            // Set all items that are not login to HAS_TOTP = false
            database.execSQL(
                """
                    UPDATE ${ItemEntity.TABLE}
                    SET ${ItemEntity.Columns.HAS_TOTP} = 0
                    WHERE ${ItemEntity.Columns.HAS_TOTP} IS NULL
                      AND ${ItemEntity.Columns.ITEM_TYPE} != ${ItemCategory.Login.value} 
                    
                """.trimIndent()
            )
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            NotificationDatabase.MIGRATION_0.migrate(database)
            NotificationDatabase.MIGRATION_1.migrate(database)
            PushDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            EventMetadataDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_26_27 = object : Migration(26, 27) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserDatabase.MIGRATION_3.migrate(database)
            AccountDatabase.MIGRATION_6.migrate(database)
        }
    }

    val MIGRATION_27_28 = object : Migration(27, 28) {
        override fun migrate(database: SupportSQLiteDatabase) {
            TelemetryDatabase.MIGRATION_0.migrate(database)
            UserSettingsDatabase.MIGRATION_3.migrate(database)
        }
    }

    @DeleteColumn.Entries(
        value = [
            DeleteColumn(
                tableName = ShareEntity.TABLE,
                columnName = ShareEntity.Columns.IS_PRIMARY
            )
        ]
    )
    class MIGRATION_34_35 : AutoMigrationSpec

    val MIGRATION_35_36 = object : Migration(35, 36) {
        override fun migrate(database: SupportSQLiteDatabase) {
            EventMetadataDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_36_37 = object : Migration(36, 37) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_4.migrate(database)
        }
    }

    @DeleteTable.Entries(value = [DeleteTable(tableName = "ProtonFeatureFlagEntity")])
    class MIGRATION_38_39 : AutoMigrationSpec

    val MIGRATION_40_41 = object : Migration(40, 41) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_5.migrate(database)
            UserKeyDatabase.MIGRATION_0.migrate(database)
            UserDatabase.MIGRATION_4.migrate(database)
            UserDatabase.MIGRATION_5.migrate(database)
            AccountDatabase.MIGRATION_7.migrate(database)
        }
    }

    val MIGRATION_42_43 = object : Migration(42, 43) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.addTableColumn(
                table = ItemEntity.TABLE,
                column = ItemEntity.Columns.HAS_PASSKEYS,
                type = "INTEGER",
                defaultValue = null
            )
            // Set all items that are not login to HAS_PASSKEYS = false
            database.execSQL(
                """
                    UPDATE ${ItemEntity.TABLE}
                    SET ${ItemEntity.Columns.HAS_PASSKEYS} = 0
                    WHERE ${ItemEntity.Columns.HAS_PASSKEYS} IS NULL
                      AND ${ItemEntity.Columns.ITEM_TYPE} != ${ItemCategory.Login.value} 
                    
                """.trimIndent()
            )
        }
    }

    val MIGRATION_43_44 = object : Migration(43, 44) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PaymentDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_44_45 = object : Migration(44, 45) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                    ALTER TABLE ${ItemEntity.TABLE}
                    ADD COLUMN ${ItemEntity.Columns.FLAGS} INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }

    val MIGRATION_46_47 = object : Migration(46, 47) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_6.migrate(database)
        }
    }

    val MIGRATION_48_49 = object : Migration(48, 49) {
        override fun migrate(db: SupportSQLiteDatabase) {
            DeviceRecoveryDatabase.MIGRATION_0.migrate(db)
            DeviceRecoveryDatabase.MIGRATION_1.migrate(db)
            UserKeyDatabase.MIGRATION_1.migrate(db)
        }
    }

    val MIGRATION_51_52 = object : Migration(51, 52) {
        override fun migrate(db: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_8.migrate(db)
            UserSettingsDatabase.MIGRATION_7.migrate(db)
            PublicAddressDatabase.MIGRATION_3.migrate(db)
            EventMetadataDatabase.MIGRATION_3.migrate(db)
        }
    }

    @RenameColumn.Entries(
        value = [
            RenameColumn(
                tableName = SecureLinkEntity.TABLE_NAME,
                fromColumnName = "is_expired",
                toColumnName = SecureLinkEntity.Columns.IS_ACTIVE
            )
        ]
    )
    class MIGRATION_52_53 : AutoMigrationSpec

    val MIGRATION_54_55 = object : Migration(54, 55) {
        override fun migrate(db: SupportSQLiteDatabase) {
            AuthDatabase.MIGRATION_0.migrate(db)
            AuthDatabase.MIGRATION_1.migrate(db)
        }
    }

    val MIGRATION_56_57 = object : Migration(56, 57) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    ALTER TABLE ${UserAccessDataEntity.TABLE}
                    ADD COLUMN ${UserAccessDataEntity.Columns.SIMPLE_LOGIN_SYNC_CAN_MANAGE_ALIAS} INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }

    val MIGRATION_58_59 = object : Migration(58, 59) {
        override fun migrate(db: SupportSQLiteDatabase) {
            AuthDatabase.MIGRATION_2.migrate(db)
            AuthDatabase.MIGRATION_3.migrate(db)
            AuthDatabase.MIGRATION_4.migrate(db)
            AuthDatabase.MIGRATION_5.migrate(db)
            UserDatabase.MIGRATION_6.migrate(db)
            AccountDatabase.MIGRATION_9.migrate(db)
        }
    }

    val MIGRATION_74_75 = object : Migration(74, 75) {
        override fun migrate(db: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_8.migrate(db)
            AccountDatabase.MIGRATION_10.migrate(db)
        }
    }

    val MIGRATION_75_76 = object : Migration(75, 76) {
        override fun migrate(db: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_4.migrate(db)
        }
    }
}
