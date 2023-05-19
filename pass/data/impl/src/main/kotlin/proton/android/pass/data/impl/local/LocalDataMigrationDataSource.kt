package proton.android.pass.data.impl.local

import kotlinx.datetime.Clock
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PassDataMigrationEntity
import javax.inject.Inject

interface LocalDataMigrationDataSource {
    suspend fun getExecutedMigrations(): List<PassDataMigrationEntity>
    suspend fun markMigrationAsExecuted(migrationName: String)
}

class LocalDataMigrationDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val clock: Clock
) : LocalDataMigrationDataSource {
    override suspend fun getExecutedMigrations(): List<PassDataMigrationEntity> =
        database.dataMigrationDao().getExecutedMigrations()

    override suspend fun markMigrationAsExecuted(migrationName: String) {
        val entity = PassDataMigrationEntity(
            id = 0, // Default value for generating the actual ID with auto increment
            name = migrationName,
            createTime = clock.now().epochSeconds
        )
        database.dataMigrationDao().insertOrUpdate(entity)
    }

}
