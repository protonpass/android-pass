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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.domain.assetlink.AssetLink
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAssetLinkRepository @Inject constructor() : AssetLinkRepository {

    private val fakeData = mutableListOf(
        AssetLink("example.com", setOf(AssetLink.Package("com.example.app", setOf("signature1")))),
        AssetLink(
            "anotherexample.com",
            setOf(AssetLink.Package("com.another.app", setOf("signature2")))
        )
    )

    override suspend fun fetch(website: String): AssetLink =
        fakeData.find { it.website == website } ?: AssetLink(website, emptySet())

    override suspend fun refreshIgnored(): List<String> = emptyList()

    override suspend fun insert(list: List<AssetLink>) {
        fakeData += list
    }

    override suspend fun purgeAll() {
        fakeData.clear()
    }

    override suspend fun purgeOlderThan(date: Instant) {
        fakeData.clear()
    }

    override fun observeByPackageName(packageName: String): Flow<List<AssetLink>> = flowOf(fakeData)
}
