package proton.android.pass.data.impl.migration

import kotlinx.coroutines.runBlocking
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.local.LocalDataMigrationDataSource
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigrator
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigratorImpl @Inject constructor(
    private val database: PassDatabase,
    private val migrationDataSource: LocalDataMigrationDataSource,
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
                PassLogger.i(TAG, "Successfully run migration ${it.migrationName}")
                migrationDataSource.markMigrationAsExecuted(it.migrationName)
            }
        }
    }

    private suspend fun getPendingMigrations(): List<Migrator> {
        val executedMigrations = migrationDataSource.getExecutedMigrations().map { it.name }
        val allMigrationNames = allMigrations.associateBy { it.migrationName }

        return allMigrations
            .filterNot { migration -> executedMigrations.contains(migration.migrationName) }
            .mapNotNull { allMigrationNames[it.migrationName] }
    }

    companion object {
        private const val TAG = "DataMigratorImpl"
    }

}
