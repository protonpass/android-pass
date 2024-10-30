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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.impl.AppCertificate
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.extensions.groupByWebsite
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEntityList
import proton.android.pass.data.impl.local.assetlink.LocalAssetLinkDataSource
import proton.android.pass.data.impl.remote.assetlink.RemoteAssetLinkDataSource
import proton.android.pass.domain.assetlink.AssetLink
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class AssetLinkRepositoryImpl @Inject constructor(
    private val remoteAssetLinkDataSource: RemoteAssetLinkDataSource,
    private val localAssetLinkDataSource: LocalAssetLinkDataSource,
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : AssetLinkRepository {

    override suspend fun fetch(website: String): AssetLink {
        PassLogger.d(TAG, "Fetching asset links for website: $website")
        val response = remoteAssetLinkDataSource.fetch(website)
        val androidAppLinks = response.filter { it.target.namespace == "android_app" }
        return androidAppLinks.toDomain(website)
    }

    override suspend fun refreshIgnored(): List<String> {
        PassLogger.d(TAG, "Refreshing ignored asset links")
        val response = remoteAssetLinkDataSource.fetchIgnored()
        localAssetLinkDataSource.refreshIgnored(response.ignoredDomains)
        return response.ignoredDomains
    }

    override suspend fun insert(list: List<AssetLink>) {
        PassLogger.d(TAG, "Inserting asset links: $list")
        localAssetLinkDataSource.insertAssetLink(list.toEntityList(clock.now()))
    }

    override suspend fun purgeAll() {
        PassLogger.d(TAG, "Purging asset links")
        localAssetLinkDataSource.purgeAll()
    }

    override suspend fun purgeOlderThan(date: Instant) {
        PassLogger.d(TAG, "Purging asset links older than: $date")
        localAssetLinkDataSource.purgeOlderThan(date)
    }

    override fun observeByPackageName(packageName: String): Flow<List<AssetLink>> =
        localAssetLinkDataSource.observeByPackageName(packageName)
            .map(List<AssetLinkEntity>::groupByWebsite)
            .map { list -> filterByCertificate(packageName, list) }

    private fun filterByCertificate(packageName: String, list: List<AssetLink>): List<AssetLink> {
        val fingerprints = AppCertificate.getAppSigningCertificates(context, packageName)
        return list.filter { it.packages.any { pkg -> pkg.packageName == packageName } }
            .filter { it.packages.any { pkg -> pkg.signatures.any { signature -> signature in fingerprints } } }
    }

    companion object {
        private const val TAG = "AssetLinkRepositoryImpl"
    }
}
