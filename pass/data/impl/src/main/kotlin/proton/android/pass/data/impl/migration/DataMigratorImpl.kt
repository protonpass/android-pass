package proton.android.pass.data.impl.migration

import kotlinx.coroutines.runBlocking
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigrator
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigratorImpl @Inject constructor(
    private val database: AppDatabase,
    itemHasTotpMigrator: ItemHasTotpMigrator
) : DataMigrator {

    private val allMigrations: List<Migrator> = listOf(
        itemHasTotpMigrator
    )

    override suspend fun areMigrationsNeeded(): Boolean {
        val migrationsToExecute = getPendingMigrations()
        return migrationsToExecute.isNotEmpty()
    }

    override fun run(): Result<Unit> = runBlocking { runCatching { runMigrations() } }

    private suspend fun runMigrations() {
        val pendingMigrations = getPendingMigrations()
        database.inTransaction {
            pendingMigrations.forEach {
                PassLogger.i(TAG, "Running migration ${it.migrationName}")
                it.migrate()
            }
        }
    }

    private suspend fun getPendingMigrations(): List<Migrator> {
        val executedMigrations = emptyList<String>()
        val allMigrationNames = allMigrations.associateBy { it.migrationName }

        return allMigrations
            .filterNot { migration -> executedMigrations.contains(migration.migrationName) }
            .mapNotNull { allMigrationNames[it.migrationName] }
    }

    companion object {
        private const val TAG = "DataMigratorImpl"
    }

}
