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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser
import proton.android.pass.test.domain.items.ItemIdMother
import proton.android.pass.test.domain.shares.ShareIdMother

internal class GetItemCategoryImplTest {

    private lateinit var itemRepository: TestItemRepository
    private lateinit var observeCurrentUser: TestObserveCurrentUser

    private lateinit var getItemCategoryImpl: GetItemCategoryImpl

    @Before
    internal fun setUp() {
        itemRepository = TestItemRepository()
        observeCurrentUser = TestObserveCurrentUser().apply {
            sendUser(TestUser.create(""))
        }

        getItemCategoryImpl = GetItemCategoryImpl(observeCurrentUser, itemRepository)
    }

    @Test
    internal fun `WHEN requesting item category THEN return item category`() = runTest {
        val shareId = ShareIdMother.create()
        val itemId = ItemIdMother.create()
        val item = TestItem.create(
            shareId = shareId,
            itemId = itemId
        )
        val expectedItemCategory = item.itemType.category
        itemRepository.setItem(item)

        val itemCategory = getItemCategoryImpl(shareId, itemId)

        assertThat(itemCategory).isEqualTo(expectedItemCategory)
    }

}
