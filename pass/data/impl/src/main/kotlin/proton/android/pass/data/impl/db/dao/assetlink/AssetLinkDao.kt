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

package proton.android.pass.data.impl.db.dao.assetlink

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.db.entities.IgnoredAssetLinkEntity

@Dao
abstract class AssetLinkDao : BaseDao<AssetLinkEntity>() {

    @Query("DELETE FROM ${AssetLinkEntity.TABLE}")
    abstract fun purge()

    @Query("DELETE FROM ${AssetLinkEntity.TABLE} WHERE ${AssetLinkEntity.Columns.CREATED_AT} < :date")
    abstract fun purgeOlderThan(date: Instant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrIgnore(entity: AssetLinkEntity)

    @Transaction
    open suspend fun insertAssetLinks(list: List<AssetLinkEntity>) {
        list.forEach { entity ->
            insertOrIgnore(entity)
        }
    }

    @Query(
        """
        SELECT * FROM ${AssetLinkEntity.TABLE}
        WHERE ${AssetLinkEntity.Columns.PACKAGE_NAME} = :packageName
        AND ${AssetLinkEntity.Columns.WEBSITE} NOT IN (
            SELECT ${IgnoredAssetLinkEntity.Columns.WEBSITE}
            FROM ${IgnoredAssetLinkEntity.TABLE}
        )
        """
    )
    abstract fun observeByPackageName(packageName: String): Flow<List<AssetLinkEntity>>
}
