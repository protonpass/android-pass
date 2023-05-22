package proton.android.pass.data.impl.migration

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.local.LocalDataMigrationDataSource
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigrator
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigratorImpl @Inject constructor(
    private val database: PassDatabase,
    private val appDatabase: AppDatabase,
    private val migrationDataSource: LocalDataMigrationDataSource,
    itemHasTotpMigrator: ItemHasTotpMigrator
) : DataMigrator {

    private val allMigrations: List<Migrator> = listOf(
        itemHasTotpMigrator
    )

    override suspend fun areMigrationsNeeded(): Boolean {
        if (!waitForRoomToBeInitialized()) {
            PassLogger.w(TAG, "Room is not initialized yet, we can't check if migrations are needed")
            return true
        }
        val migrationsToExecute = getPendingMigrations()
        return migrationsToExecute.isNotEmpty()
    }

    override fun run(): Result<Unit> = runBlocking { runCatching { runMigrations() } }

    private suspend fun runMigrations() {
        if (!waitForRoomToBeInitialized()) {
            PassLogger.w(TAG, "Room is not initialized yet, we can't perform any operation")
            return
        }
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

    private suspend fun waitForRoomToBeInitialized(): Boolean {
        var retryDelay = DATABASE_OPEN_RETRY_DELAY
        for (i in 0..MAX_DATABASE_OPEN_RETRIES) {
            // Once this call finishes we can be sure that Room is initialized
            val database = appDatabase.openHelper.readableDatabase
            if (database.isOpen) {
                return true
            }
            delay(retryDelay)
            retryDelay *= 2
        }
        return false
    }

    companion object {
        private const val TAG = "DataMigratorImpl"

        private const val MAX_DATABASE_OPEN_RETRIES = 10
        private const val DATABASE_OPEN_RETRY_DELAY = 100L
    }

}
