/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.usecases.assetlink

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.domain.assetlink.AssetLink
import java.util.concurrent.atomic.AtomicInteger

class UpdateAssetLinkImplTest {

    @Test
    fun `persists each fetched asset link before fetching the next website`() = runTest {
        val repository = FakeAssetLinkRepository()
        val instance = UpdateAssetLinkImpl(
            assetLinkRepository = repository,
            domainManager = object : DomainManager {
                override fun getRoot(url: String): String = url.removePrefix("https://")
            }
        )

        instance((1..21).map { "https://website-$it.test" }.toSet())

        assertThat(repository.insertInvocations.map(List<AssetLink>::size))
            .containsExactlyElementsIn(List(21) { 1 })
            .inOrder()
    }

    @Test
    fun `fetches one asset-link response at a time`() = runTest {
        val activeRequests = AtomicInteger(0)
        val maximumActiveRequests = AtomicInteger(0)
        val repository = FakeAssetLinkRepository().apply {
            onFetch = {
                val active = activeRequests.incrementAndGet()
                maximumActiveRequests.updateAndGet { maxOf(it, active) }
                delay(1)
                activeRequests.decrementAndGet()
            }
        }
        val instance = UpdateAssetLinkImpl(
            assetLinkRepository = repository,
            domainManager = object : DomainManager {
                override fun getRoot(url: String): String = url.removePrefix("https://")
            }
        )

        instance((1..10).map { "https://website-$it.test" }.toSet())

        assertThat(maximumActiveRequests.get()).isEqualTo(1)
    }
}
