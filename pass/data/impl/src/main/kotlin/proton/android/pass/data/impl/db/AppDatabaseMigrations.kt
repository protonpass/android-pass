package proton.android.pass.data.impl.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.payment.data.local.db.PaymentDatabase

@Suppress("MagicNumber")
object AppDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_1.migrate(database)
            FeatureFlagDatabase.MIGRATION_2.migrate(database)
            FeatureFlagDatabase.MIGRATION_3.migrate(database)
            HumanVerificationDatabase.MIGRATION_1.migrate(database)
            HumanVerificationDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PaymentDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PassDatabase.MIGRATION_4.migrate(database)
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_5.migrate(database)
        }
    }
}
