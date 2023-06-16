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
