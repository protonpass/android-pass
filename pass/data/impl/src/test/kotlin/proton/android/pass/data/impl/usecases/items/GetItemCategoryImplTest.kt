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
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.UserTestFactory
import proton.android.pass.test.domain.items.ItemIdTestFactory
import proton.android.pass.test.domain.shares.ShareIdTestFactory

internal class GetItemCategoryImplTest {

    private lateinit var itemRepository: FakeItemRepository
    private lateinit var observeCurrentUser: FakeObserveCurrentUser

    private lateinit var getItemCategoryImpl: GetItemCategoryImpl

    @Before
    internal fun setUp() {
        itemRepository = FakeItemRepository()
        observeCurrentUser = FakeObserveCurrentUser().apply {
            sendUser(UserTestFactory.create(""))
        }

        getItemCategoryImpl = GetItemCategoryImpl(observeCurrentUser, itemRepository)
    }

    @Test
    internal fun `WHEN requesting item category THEN return item category`() = runTest {
        val shareId = ShareIdTestFactory.create()
        val itemId = ItemIdTestFactory.create()
        val item = ItemTestFactory.create(
            shareId = shareId,
            itemId = itemId
        )
        val expectedItemCategory = item.itemType.category
        itemRepository.setItem(item)

        val itemCategory = getItemCategoryImpl(shareId, itemId)

        assertThat(itemCategory).isEqualTo(expectedItemCategory)
    }

}
