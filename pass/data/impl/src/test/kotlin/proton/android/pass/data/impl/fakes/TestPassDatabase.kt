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

package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.dao.FeatureFlagsDao
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassDataMigrationDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.PlanDao
import proton.android.pass.data.impl.db.dao.SearchEntryDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.SharesDao
import proton.android.pass.data.impl.db.dao.TelemetryDao

class TestPassDatabase : PassDatabase {
    override fun sharesDao(): SharesDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun itemsDao(): ItemsDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun shareKeysDao(): ShareKeysDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun passEventsDao(): PassEventsDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun telemetryEventsDao(): TelemetryDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun searchEntryDao(): SearchEntryDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun planDao(): PlanDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun dataMigrationDao(): PassDataMigrationDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun featureFlagsDao(): FeatureFlagsDao {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()
}
