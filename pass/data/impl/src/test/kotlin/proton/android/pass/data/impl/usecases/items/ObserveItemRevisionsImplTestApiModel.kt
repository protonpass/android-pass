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

package proton.android.pass.data.impl.usecases.items

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.test.domain.items.ItemIdMother
import proton.android.pass.test.domain.shares.ShareIdMother

internal class ObserveItemRevisionsImplTestApiModel {

    private lateinit var accountManager: FakeAccountManager
    private lateinit var itemRepository: FakeItemRepository

    private lateinit var observeItemRevisionsImpl: ObserveItemRevisionsImpl

    @Before
    internal fun setUp() {
        accountManager = FakeAccountManager()
        itemRepository = FakeItemRepository()

        observeItemRevisionsImpl = ObserveItemRevisionsImpl(
            accountManager = accountManager,
            itemRepository = itemRepository
        )
    }

    @Test
    internal fun `WHEN observing item revisions THEN emit item revisions`() = runTest {
        val shareId = ShareIdMother.create()
        val itemId = ItemIdMother.create()
        val expectedItemRevisions = emptyList<ItemRevision>()
        itemRepository.setItemRevisions(expectedItemRevisions)

        observeItemRevisionsImpl(shareId, itemId).test {
            val itemRevisions = awaitItem()

            assertThat(itemRevisions).isEqualTo(expectedItemRevisions)
        }
    }

}
