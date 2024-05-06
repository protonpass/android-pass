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

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.challenge.data.db.ChallengeConverters
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.challenge.data.entity.ChallengeFrameEntity
import me.proton.core.crypto.android.keystore.CryptoConverters
import me.proton.core.data.room.db.BaseDatabase
import me.proton.core.data.room.db.CommonConverters
import me.proton.core.eventmanager.data.db.EventManagerConverters
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.humanverification.data.db.HumanVerificationConverters
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.humanverification.data.entity.HumanVerificationEntity
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.data.entity.PublicAddressEntity
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.keytransparency.data.local.entity.AddressChangeEntity
import me.proton.core.keytransparency.data.local.entity.SelfAuditResultEntity
import me.proton.core.notification.data.local.db.NotificationConverters
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.local.db.NotificationEntity
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.ObservabilityEventEntity
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.entity.GooglePurchaseEntity
import me.proton.core.payment.data.local.entity.PurchaseEntity
import me.proton.core.push.data.local.db.PushConverters
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.local.db.PushEntity
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.telemetry.data.entity.TelemetryEventEntity
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserConverters
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsConverters
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.entity.OrganizationEntity
import me.proton.core.usersettings.data.entity.OrganizationKeysEntity
import me.proton.core.usersettings.data.entity.UserSettingsEntity
import proton.android.pass.data.impl.db.entities.InviteEntity
import proton.android.pass.data.impl.db.entities.InviteKeyEntity
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.db.entities.PassDataMigrationEntity
import proton.android.pass.data.impl.db.entities.PassEventEntity
import proton.android.pass.data.impl.db.entities.PassOrganizationSettingsEntity
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.db.entities.SearchEntryEntity
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.db.entities.UserAccessDataEntity

@Database(
    entities = [
        // Core
        AccountEntity::class,
        AccountMetadataEntity::class,
        AddressChangeEntity::class,
        AddressEntity::class,
        AddressKeyEntity::class,
        ChallengeFrameEntity::class,
        EventMetadataEntity::class,
        FeatureFlagEntity::class,
        GooglePurchaseEntity::class,
        PurchaseEntity::class,
        HumanVerificationEntity::class,
        KeySaltEntity::class,
        OrganizationEntity::class,
        OrganizationKeysEntity::class,
        ObservabilityEventEntity::class,
        PublicAddressEntity::class,
        PublicAddressKeyEntity::class,
        SelfAuditResultEntity::class,
        SessionDetailsEntity::class,
        SessionEntity::class,
        UserEntity::class,
        UserKeyEntity::class,
        UserSettingsEntity::class,
        NotificationEntity::class,
        PushEntity::class,
        TelemetryEventEntity::class,
        // Pass
        ItemEntity::class,
        ShareEntity::class,
        ShareKeyEntity::class,
        PassEventEntity::class,
        TelemetryEntity::class,
        SearchEntryEntity::class,
        PlanEntity::class,
        PassDataMigrationEntity::class,
        InviteEntity::class,
        InviteKeyEntity::class,
        UserAccessDataEntity::class,
        PassOrganizationSettingsEntity::class
    ],
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = AppDatabaseMigrations.MIGRATION_2_3::class),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12, spec = AppDatabaseMigrations.MIGRATION_11_12::class),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 28, to = 29),
        AutoMigration(from = 29, to = 30),
        AutoMigration(from = 30, to = 31),
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
        AutoMigration(from = 33, to = 34),
        AutoMigration(from = 34, to = 35, spec = AppDatabaseMigrations.MIGRATION_34_35::class),
        AutoMigration(from = 37, to = 38),
        AutoMigration(from = 38, to = 39, spec = AppDatabaseMigrations.MIGRATION_38_39::class),
        AutoMigration(from = 39, to = 40),
        AutoMigration(from = 41, to = 42),
        AutoMigration(from = 45, to = 46)
    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(
    // Core
    CommonConverters::class,
    AccountConverters::class,
    UserConverters::class,
    CryptoConverters::class,
    HumanVerificationConverters::class,
    UserSettingsConverters::class,
    EventManagerConverters::class,
    ChallengeConverters::class,
    NotificationConverters::class,
    PushConverters::class
)
abstract class AppDatabase :
    BaseDatabase(),
    AccountDatabase,
    AddressDatabase,
    ChallengeDatabase,
    EventMetadataDatabase,
    FeatureFlagDatabase,
    HumanVerificationDatabase,
    KeySaltDatabase,
    KeyTransparencyDatabase,
    OrganizationDatabase,
    ObservabilityDatabase,
    PassDatabase,
    PaymentDatabase,
    PublicAddressDatabase,
    UserDatabase,
    UserSettingsDatabase,
    NotificationDatabase,
    PushDatabase,
    TelemetryDatabase {

    companion object {
        const val VERSION = 47

        const val DB_NAME = "db-passkey"

        val migrations: List<Migration> = listOf(
            AppDatabaseMigrations.MIGRATION_1_2,
            AppDatabaseMigrations.MIGRATION_6_7,
            AppDatabaseMigrations.MIGRATION_7_8,
            AppDatabaseMigrations.MIGRATION_8_9,
            AppDatabaseMigrations.MIGRATION_9_10,
            AppDatabaseMigrations.MIGRATION_14_15,
            AppDatabaseMigrations.MIGRATION_16_17,
            AppDatabaseMigrations.MIGRATION_17_18,
            AppDatabaseMigrations.MIGRATION_18_19,
            AppDatabaseMigrations.MIGRATION_22_23,
            AppDatabaseMigrations.MIGRATION_26_27,
            AppDatabaseMigrations.MIGRATION_27_28,
            AppDatabaseMigrations.MIGRATION_35_36,
            AppDatabaseMigrations.MIGRATION_36_37,
            AppDatabaseMigrations.MIGRATION_40_41,
            AppDatabaseMigrations.MIGRATION_42_43,
            AppDatabaseMigrations.MIGRATION_43_44,
            AppDatabaseMigrations.MIGRATION_44_45,
            AppDatabaseMigrations.MIGRATION_46_47
        )

        fun buildDatabase(context: Context): AppDatabase = databaseBuilder<AppDatabase>(context, DB_NAME)
            .apply { migrations.forEach { addMigrations(it) } }
            .fallbackToDestructiveMigration()
            .build()
    }
}
