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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEntity
import proton.android.pass.data.impl.local.assetlink.LocalAssetLinkDataSource
import proton.android.pass.data.impl.remote.assetlink.RemoteAssetLinkDataSource
import proton.android.pass.domain.assetlink.AssetLink
import javax.inject.Inject

class AssetLinkRepositoryImpl @Inject constructor(
    private val remoteAssetLinkDataSource: RemoteAssetLinkDataSource,
    private val localAssetLinkDataSource: LocalAssetLinkDataSource
) : AssetLinkRepository {

    override suspend fun fetch(website: String): List<AssetLink> = remoteAssetLinkDataSource.fetch(website)

    override suspend fun insert(list: List<AssetLink>) {
        localAssetLinkDataSource.insertAssetLink(list.map(AssetLink::toEntity))
    }

    override suspend fun purge() {
        localAssetLinkDataSource.purge()
    }

    override fun observe(website: String): Flow<List<AssetLink>> =
        localAssetLinkDataSource.observeAssetLinks(website).map { list ->
            list.map(AssetLinkEntity::toDomain)
        }
}
