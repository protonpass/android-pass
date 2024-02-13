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

package proton.android.pass.features.item.history.timeline.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.fakes.usecases.items.FakeGetItemCategory
import proton.android.pass.data.fakes.usecases.items.FakeObserveItemRevisions
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.items.ItemCategoryMother
import proton.android.pass.test.domain.items.ItemIdMother
import proton.android.pass.test.domain.shares.ShareIdMother

internal class ItemHistoryTimelineViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider
    private lateinit var observeItemRevisions: FakeObserveItemRevisions
    private lateinit var getItemCategory: FakeGetItemCategory

    @Before
    internal fun setUp() {
        savedStateHandleProvider = TestSavedStateHandleProvider()
        observeItemRevisions = FakeObserveItemRevisions()
        getItemCategory = FakeGetItemCategory()

        savedStateHandleProvider.get().apply {
            set(CommonNavArgId.ShareId.key, ShareIdMother.create().id)
            set(CommonNavArgId.ItemId.key, ItemIdMother.create().id)
        }
    }

    @Test
    internal fun `WHEN view model is initialized THEN emit initial state`() = runTest {
        val expectedInitialState = ItemHistoryTimelineStateMother.create()

        val viewModel = createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()

            assertThat(initialState).isEqualTo(expectedInitialState)
        }
    }

    @Test
    internal fun `WHEN item revisions AND item category are emitted THEN update state`() = runTest {
        val itemRevisions = emptyList<ItemRevision>()
        val itemCategory = ItemCategoryMother.random()
        val expectedUpdatedState = ItemHistoryTimelineStateMother.create(
            isLoadingState = IsLoadingState.NotLoading,
            itemRevisions = itemRevisions,
            itemCategory = itemCategory,
        )
        observeItemRevisions.setItemRevisions(itemRevisions)
        getItemCategory.setItemCategory(itemCategory)

        val viewModel = createViewModel()

        viewModel.state.test {
            val updatedState = awaitItem()

            assertThat(updatedState).isEqualTo(expectedUpdatedState)
        }
    }

    private fun createViewModel() = ItemHistoryTimelineViewModel(
        savedStateHandleProvider = savedStateHandleProvider,
        observeItemRevisions = observeItemRevisions,
        getItemCategory = getItemCategory,
    )

}
