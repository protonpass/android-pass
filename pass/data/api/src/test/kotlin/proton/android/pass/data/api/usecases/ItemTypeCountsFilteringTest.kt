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

package proton.android.pass.data.api.usecases

import org.junit.Test
import proton.android.pass.data.api.repositories.ItemTypeCounts
import kotlin.test.assertEquals

class ItemTypeCountsFilteringTest {

    private val counts = ItemTypeCounts(
        loginCount = 5,
        loginWithTotpCount = 2,
        aliasCount = 4,
        noteCount = 3,
        creditCardCount = 2,
        identityCount = 6,
        customCount = 1
    )

    @Test
    fun `All sums every item type`() {
        assertEquals(21, counts.totalFor(ItemTypeFilter.All))
    }

    @Test
    fun `Logins counts only login items`() {
        assertEquals(5, counts.totalFor(ItemTypeFilter.Logins))
    }

    @Test
    fun `LoginWithTotp counts only login items with TOTP`() {
        assertEquals(2, counts.totalFor(ItemTypeFilter.LoginWithTotp))
    }

    @Test
    fun `Aliases counts only alias items`() {
        assertEquals(4, counts.totalFor(ItemTypeFilter.Aliases))
    }

    @Test
    fun `Notes counts only note items`() {
        assertEquals(3, counts.totalFor(ItemTypeFilter.Notes))
    }

    @Test
    fun `CreditCards counts only credit card items`() {
        assertEquals(2, counts.totalFor(ItemTypeFilter.CreditCards))
    }

    @Test
    fun `Identity counts only identity items`() {
        assertEquals(6, counts.totalFor(ItemTypeFilter.Identity))
    }

    @Test
    fun `Custom counts only custom items`() {
        assertEquals(1, counts.totalFor(ItemTypeFilter.Custom))
    }

    @Test
    fun `empty counts return zero for every filter`() {
        ItemTypeFilter.entries.forEach { filter ->
            assertEquals(0, ItemTypeCounts.EMPTY.totalFor(filter))
        }
    }
}
