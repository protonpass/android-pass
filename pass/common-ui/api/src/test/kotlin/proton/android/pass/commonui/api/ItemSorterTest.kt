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

package proton.android.pass.commonui.api

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.fail
import org.junit.Test
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleDesc
import proton.android.pass.commonuimodels.fakes.TestItemUiModel

class ItemSorterTest {

    @Test
    fun `given a list of items sort by title desc`() {
        val itemList = listOfTestItemUiModels()
        val sortingKeyA = GroupingKeys.AlphabeticalKey('A')
        val sortingKeyB = GroupingKeys.AlphabeticalKey('B')
        val sortingKeyS = GroupingKeys.AlphabeticalKey('#')
        val sorted = itemList.groupAndSortByTitleDesc()
        assertThat(sorted).isNotEmpty()
        assertThat(sorted).hasSize(3)

        val sortedA = sorted.first { it.key == sortingKeyA }
        val sortedB = sorted.first { it.key == sortingKeyB }
        val sortedS = sorted.first { it.key == sortingKeyS }
        assertThat(sortedA).isNotNull()
        assertThat(sortedB).isNotNull()
        assertThat(sortedS).isNotNull()
        assertThat(sortedA.items).hasSize(1)
        assertThat(sortedB.items).hasSize(1)
        assertThat(sortedS.items).hasSize(2)

        val keys = sorted.map { it.key }
        assert(keys == listOf(sortingKeyB, sortingKeyA, sortingKeyS))
    }

    @Test
    fun `given a list of items sort by title asc`() {
        val itemList = listOfTestItemUiModels()
        val sortingKeyA = GroupingKeys.AlphabeticalKey('A')
        val sortingKeyB = GroupingKeys.AlphabeticalKey('B')
        val sortingKeyS = GroupingKeys.AlphabeticalKey('#')
        val sorted = itemList.groupAndSortByTitleAsc()
        assertThat(sorted).isNotEmpty()
        assertThat(sorted).hasSize(3)


        val sortedA = sorted.first { it.key == sortingKeyA }
        val sortedB = sorted.first { it.key == sortingKeyB }
        val sortedS = sorted.first { it.key == sortingKeyS }
        assertThat(sortedA).isNotNull()
        assertThat(sortedB).isNotNull()
        assertThat(sortedS).isNotNull()
        assertThat(sortedA.items).hasSize(1)
        assertThat(sortedB.items).hasSize(1)
        assertThat(sortedS.items).hasSize(2)

        val keys = sorted.map { it.key }
        assert(keys == listOf(sortingKeyS, sortingKeyA, sortingKeyB))
    }

    @Test
    fun `given a list of items sort by created date asc`() {
        val itemList = listOfTestItemUiModels()
        val sortingKeyJanuary =
            GroupingKeys.MonthlyKey(JANUARY_2010, item1Jan().createTime)
        val sortingKeyAugust =
            GroupingKeys.MonthlyKey(AUGUST_2019, item1Aug().createTime)
        val sortingKeyFebruary =
            GroupingKeys.MonthlyKey(FEBRUARY_2022, item1Feb().createTime)
        val sorted = itemList.groupAndSortByCreationAsc()
        assertThat(sorted).isNotEmpty()
        assertThat(sorted).hasSize(3)

        val sortedJanuary = sorted.first { it.key == sortingKeyJanuary }
        val sortedAugust = sorted.first { it.key == sortingKeyAugust }
        val sortedFebruary = sorted.first { it.key == sortingKeyFebruary }
        assertThat(sortedJanuary).isNotNull()
        assertThat(sortedAugust).isNotNull()
        assertThat(sortedFebruary).isNotNull()
        assertThat(sortedJanuary.items).hasSize(1)
        assertThat(sortedAugust.items).hasSize(1)
        assertThat(sortedFebruary.items).hasSize(2)

        val keys = sorted.map { it.key }
        assert(keys == listOf(sortingKeyJanuary, sortingKeyAugust, sortingKeyFebruary))

        assert(sortedFebruary.items[0].createTime == item1Feb().createTime)
        assert(sortedFebruary.items[1].createTime == item15Feb().createTime)
    }

    @Test
    fun `given a list of items sort by created date desc`() {
        val itemList = listOfTestItemUiModels()
        val sortingKeyJanuary =
            GroupingKeys.MonthlyKey(JANUARY_2010, item1Jan().createTime)
        val sortingKeyAugust =
            GroupingKeys.MonthlyKey(AUGUST_2019, item1Aug().createTime)
        val sortingKeyFebruary =
            GroupingKeys.MonthlyKey(FEBRUARY_2022, item1Feb().createTime)
        val sorted = itemList.groupAndSortByCreationDesc()
        assertThat(sorted).isNotEmpty()
        assertThat(sorted).hasSize(3)

        val sortedJanuary = sorted.first { it.key == sortingKeyJanuary }
        val sortedAugust = sorted.first { it.key == sortingKeyAugust }
        val sortedFebruary = sorted.first { it.key == sortingKeyFebruary }
        assertThat(sortedJanuary).isNotNull()
        assertThat(sortedAugust).isNotNull()
        assertThat(sortedFebruary).isNotNull()
        assertThat(sortedJanuary.items).hasSize(1)
        assertThat(sortedAugust.items).hasSize(1)
        assertThat(sortedFebruary.items).hasSize(2)

        val keys = sorted.map { it.key }
        assert(keys == listOf(sortingKeyFebruary, sortingKeyAugust, sortingKeyJanuary))

        assert(sortedFebruary.items[0].createTime == item15Feb().createTime)
        assert(sortedFebruary.items[1].createTime == item1Feb().createTime)
    }

    @Test
    fun `given a list of items sort by most recent`() {
        val itemList = listOfMostRecentTestItemUiModels()
        val now = Instant.fromEpochSeconds(NOW)
        val sortingKeyToday = GroupingKeys.MostRecentKey(
            formatResultKey = DateFormatUtils.Format.Today,
            instant = itemToday().modificationTime
        )
        val sorted = itemList.groupAndSortByMostRecent(now)
        assertThat(sorted).isNotEmpty()
        assertThat(sorted).hasSize(3)
        sorted.onEachIndexed { index, entry ->
            when (index) {
                0 -> assertThat((entry.key as GroupingKeys.MostRecentKey).formatResultKey)
                    .isInstanceOf(DateFormatUtils.Format.Today::class.java)
                1 -> assertThat((entry.key as GroupingKeys.MostRecentKey).formatResultKey)
                    .isInstanceOf(DateFormatUtils.Format.Yesterday::class.java)
                2 -> assertThat((entry.key as GroupingKeys.MostRecentKey).formatResultKey)
                    .isInstanceOf(DateFormatUtils.Format.ThisWeek::class.java)
                else -> fail("Unhandled")
            }
        }

        val sortedToday = sorted.first { it.key == sortingKeyToday }
        assertThat(sortedToday).isNotNull()

        assertThat(sortedToday.items[0].modificationTime).isEqualTo(itemToday().modificationTime)
        assertThat(sortedToday.items[1].lastAutofillTime).isEqualTo(itemAutofillToday().lastAutofillTime)
    }

    private fun item15Feb() = TestItemUiModel.create(
        title = "15",
        createTime = LocalDateTime(2022, 2, 15, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun item1Feb() = TestItemUiModel.create(
        title = "$",
        createTime = LocalDateTime(2022, 2, 15, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun item1Aug() = TestItemUiModel.create(
        title = "A",
        createTime = LocalDateTime(2019, 8, 1, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun item1Jan() = TestItemUiModel.create(
        title = "B",
        createTime = LocalDateTime(2010, 1, 1, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun listOfTestItemUiModels() =
        listOf(item15Feb(), item1Feb(), item1Aug(), item1Jan())

    private fun listOfMostRecentTestItemUiModels() =
        listOf(itemYesterday(), itemToday(), itemAutofillToday(), itemThisWeek())

    private fun itemAutofillToday() = TestItemUiModel.create(
        title = "Today",
        modificationTime = LocalDateTime(2020, 2, 17, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC),
        lastAutofillTime = LocalDateTime(2023, 2, 17, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun itemThisWeek() = TestItemUiModel.create(
        title = "This week",
        modificationTime = LocalDateTime(2023, 2, 15, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun itemToday() = TestItemUiModel.create(
        title = "Today",
        modificationTime = LocalDateTime(2023, 2, 17, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    private fun itemYesterday() = TestItemUiModel.create(
        title = "Yesterday",
        modificationTime = LocalDateTime(2023, 2, 16, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
    )

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val JANUARY_2010 = "January 2010"
        private const val AUGUST_2019 = "August 2019"
        private const val FEBRUARY_2022 = "February 2022"
        private const val NOW = 1676641715L // Friday, February 17, 2023 13:48:35 UTC
    }
}
