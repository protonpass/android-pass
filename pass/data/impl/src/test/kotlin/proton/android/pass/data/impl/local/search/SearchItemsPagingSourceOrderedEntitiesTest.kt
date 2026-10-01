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

package proton.android.pass.data.impl.local.search

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.mother.ItemEntityTestFactory
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

internal class SearchItemsPagingSourceOrderedEntitiesTest {

    private lateinit var localItemDataSource: FakeLocalItemDataSource

    @Before
    fun setup() {
        localItemDataSource = FakeLocalItemDataSource()
    }

    @Test
    fun `rows from several users are hydrated per user in search order`() = runTest {
        localItemDataSource.upsertItems(
            listOf(
                ItemEntityTestFactory.create(id = ITEM_A1, userId = USER_A, shareId = SHARE_A),
                ItemEntityTestFactory.create(id = ITEM_A2, userId = USER_A, shareId = SHARE_A),
                ItemEntityTestFactory.create(id = ITEM_B1, userId = USER_B, shareId = SHARE_B)
            )
        )
        val rows = listOf(
            SearchIdRow(userId = USER_B, shareId = SHARE_B, itemId = ITEM_B1),
            SearchIdRow(userId = USER_A, shareId = SHARE_A, itemId = ITEM_A2),
            SearchIdRow(userId = USER_A, shareId = SHARE_A, itemId = ITEM_A1)
        )

        val result = localItemDataSource.getOrderedEntities(rows)

        assertThat(result.map { it.userId to it.id }).containsExactly(
            USER_B to ITEM_B1,
            USER_A to ITEM_A2,
            USER_A to ITEM_A1
        ).inOrder()
        assertThat(localItemDataSource.getByShareItemPairsCalls()).containsExactly(
            UserId(USER_B) to listOf(ShareId(SHARE_B) to ItemId(ITEM_B1)),
            UserId(USER_A) to listOf(ShareId(SHARE_A) to ItemId(ITEM_A2), ShareId(SHARE_A) to ItemId(ITEM_A1))
        )
    }

    @Test
    fun `rows without a local item are skipped`() = runTest {
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(id = ITEM_A1, userId = USER_A, shareId = SHARE_A)
        )
        val rows = listOf(
            SearchIdRow(userId = USER_B, shareId = SHARE_B, itemId = ITEM_B1),
            SearchIdRow(userId = USER_A, shareId = SHARE_A, itemId = ITEM_A1)
        )

        val result = localItemDataSource.getOrderedEntities(rows)

        assertThat(result.map { it.id }).containsExactly(ITEM_A1)
    }

    @Test
    fun `no rows returns no entities without querying`() = runTest {
        val result = localItemDataSource.getOrderedEntities(emptyList())

        assertThat(result).isEmpty()
        assertThat(localItemDataSource.getByShareItemPairsCalls()).isEmpty()
    }

    private companion object {
        private const val USER_A = "user-a"
        private const val USER_B = "user-b"
        private const val SHARE_A = "share-a"
        private const val SHARE_B = "share-b"
        private const val ITEM_A1 = "item-a1"
        private const val ITEM_A2 = "item-a2"
        private const val ITEM_B1 = "item-b1"
    }
}
