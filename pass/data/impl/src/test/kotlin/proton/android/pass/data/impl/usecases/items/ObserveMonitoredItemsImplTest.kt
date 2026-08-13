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

package proton.android.pass.data.impl.usecases.items

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.test.domain.ItemTestFactory

internal class ObserveMonitoredItemsImplTest {

    private lateinit var observeItems: FakeObserveItems

    private lateinit var instance: ObserveMonitoredItemsImpl

    @Before
    fun setup() {
        observeItems = FakeObserveItems()
        instance = ObserveMonitoredItemsImpl(observeItems = observeItems)
    }

    @Test
    fun `keeps items that are not excluded from monitoring`() = runTest {
        val item = itemWith(id = "monitored")
        observeItems.emitValue(listOf(item))

        val result = instance(includeHiddenVaults = false).first()

        assertThat(result).containsExactly(item)
    }

    @Test
    fun `filters out items excluded from monitoring`() = runTest {
        val item = itemWith(id = "excluded", flags = listOf(ItemFlag.SkipHealthCheck))
        observeItems.emitValue(listOf(item))

        val result = instance(includeHiddenVaults = false).first()

        assertThat(result).isEmpty()
    }

    @Test
    fun `keeps excluded items that still have a restored check`() = runTest {
        val item = itemWith(
            id = "partially-restored",
            flags = listOf(ItemFlag.SkipHealthCheck, ItemFlag.Skip2FACheck)
        )
        observeItems.emitValue(listOf(item))

        val result = instance(includeHiddenVaults = false).first()

        assertThat(result).containsExactly(item)
    }

    @Test
    fun `keeps items that only have ignored checks`() = runTest {
        val item = itemWith(id = "ignored-check", flags = listOf(ItemFlag.SkipWeakPasswordCheck))
        observeItems.emitValue(listOf(item))

        val result = instance(includeHiddenVaults = false).first()

        assertThat(result).containsExactly(item)
    }

    private fun itemWith(id: String, flags: List<ItemFlag> = emptyList()): Item = ItemTestFactory.createLogin(
        itemId = ItemId(id),
        flags = flags.sumOf { flag -> flag.value }
    )
}
