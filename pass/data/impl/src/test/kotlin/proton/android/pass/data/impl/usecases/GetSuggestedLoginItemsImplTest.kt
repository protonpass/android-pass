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

package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveUsableVaults
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private typealias Filter = (Item) -> Boolean

internal class FakeSuggestionItemFilterer : SuggestionItemFilterer {
    private var filter: Filter = {
        throw IllegalStateException("Filter has not been initialized")
    }

    fun setFilter(fn: Filter) {
        this.filter = fn
    }

    override fun filter(
        items: List<Item>,
        packageName: Option<String>,
        url: Option<String>
    ): List<Item> = items.filter { filter.invoke(it) }
}

class FakeSuggestionSorter : SuggestionSorter {
    override fun sort(
        items: List<Item>,
        url: Option<String>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<Item> = items
}

@RunWith(JUnit4::class)
class GetSuggestedLoginItemsImplTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var observeActiveItems: TestObserveActiveItems
    private lateinit var filter: FakeSuggestionItemFilterer
    private lateinit var getSuggestedLoginItems: GetSuggestedLoginItems
    private lateinit var observeVaults: TestObserveUsableVaults
    private lateinit var internalSettingsRepository: TestInternalSettingsRepository

    @Before
    fun setUp() {
        observeActiveItems = TestObserveActiveItems()
        filter = FakeSuggestionItemFilterer()
        observeVaults = TestObserveUsableVaults()
        internalSettingsRepository = TestInternalSettingsRepository()
        getSuggestedLoginItems = GetSuggestedLoginItemsImpl(
            observeUsableVaults = observeVaults,
            observeActiveItems = observeActiveItems,
            suggestionItemFilter = filter,
            suggestionSorter = FakeSuggestionSorter(),
            internalSettingsRepository = internalSettingsRepository
        )
    }

    @Test
    fun `filter is invoked`() = runTest {
        emitDefaultVault()

        val fixedTitle = "item1"
        val item1 = TestItem.random(title = fixedTitle)
        val item2 = TestItem.random()


        observeActiveItems.sendItemList(listOf(item1, item2))
        filter.setFilter { TestKeyStoreCrypto.decrypt(it.title) == fixedTitle }

        getSuggestedLoginItems.invoke(None, None).test {
            assertEquals(awaitItem(), listOf(item1))
        }

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = ShareSelection.AllShares
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `error is propagated`() = runTest {
        emitDefaultVault()

        val message = "test exception"

        filter.setFilter { true }
        observeActiveItems.sendException(Exception(message))

        getSuggestedLoginItems.invoke(None, None).test {
            val e = awaitError()
            assertTrue(e is Exception)
            assertEquals(e.message, message)
        }

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = ShareSelection.AllShares
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `only suggestions from the usable vaults`() = runTest {
        // GIVEN
        val firstShareId = ShareId("123")
        val secondShareId = ShareId("456")
        val vaults = listOf(
            Vault(
                userId = UserId(""),
                shareId = firstShareId,
                name = "default",
                role = ShareRole.Admin,
                createTime = Date()
            ),
            Vault(
                userId = UserId(""),
                shareId = secondShareId,
                name = "other",
                role = ShareRole.Admin,
                createTime = Date()
            ),
            Vault(
                userId = UserId(""),
                shareId = ShareId("789"),
                name = "another",
                role = ShareRole.Read,
                createTime = Date()
            )
        )

        val shareSelection = ShareSelection.Shares(vaults.map { it.shareId })
        observeVaults.emit(Result.success(shareSelection))

        filter.setFilter { true }

        val items = listOf(TestItem.random())
        observeActiveItems.sendItemList(items)

        // WHEN
        val res = getSuggestedLoginItems.invoke(None, None).first()

        // THEN
        assertThat(res).isEqualTo(items)

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = shareSelection
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    private fun emitDefaultVault() {
        observeVaults.emit(Result.success(ShareSelection.AllShares))
    }
}

