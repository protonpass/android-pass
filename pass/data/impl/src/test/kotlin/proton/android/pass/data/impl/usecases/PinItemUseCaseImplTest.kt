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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestItem

internal class PinItemUseCaseImplTest {

    private lateinit var itemRepository: TestItemRepository

    private lateinit var pinItemUseCase: PinItemUseCaseImpl

    @Before
    internal fun setup() {
        itemRepository = TestItemRepository()

        pinItemUseCase = PinItemUseCaseImpl(itemRepository)
    }

    @Test
    internal fun `GIVEN item id AND share id WHEN pinning an item THEN return pinned item`() =
        runTest {
            val itemId = ItemId(id = TestUtils.randomString())
            val sharedId = ShareId(id = TestUtils.randomString())
            val newItem = TestItem.random()
            val expectedItem = newItem.copy(id = itemId, shareId = sharedId, isPinned = true)
            itemRepository.setItem(newItem)

            val item = pinItemUseCase.execute(sharedId, itemId)

            assertThat(item).isEqualTo(expectedItem)
        }

}
