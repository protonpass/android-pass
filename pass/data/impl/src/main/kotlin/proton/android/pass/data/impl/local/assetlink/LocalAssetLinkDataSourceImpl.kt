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

package proton.android.pass.data.impl.local.assetlink

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.db.entities.IgnoredAssetLinkEntity
import javax.inject.Inject

class LocalAssetLinkDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalAssetLinkDataSource {

    override suspend fun insertAssetLink(list: List<AssetLinkEntity>) {
        database.assetLinkDao().insertAssetLinks(list)
    }

    override suspend fun purgeAll() {
        database.assetLinkDao().purge()
    }

    override suspend fun purgeOlderThan(date: Instant) {
        database.assetLinkDao().purgeOlderThan(date)
    }

    override fun observeByPackageName(packageName: String): Flow<List<AssetLinkEntity>> =
        database.assetLinkDao().observeByPackageName(packageName)

    override suspend fun refreshIgnored(ignoredDomains: List<String>) {
        database.ignoredAssetLinkDao().deleteAll()
        database.ignoredAssetLinkDao().insertOrUpdate(*ignoredDomains.map(::IgnoredAssetLinkEntity).toTypedArray())
    }

}
