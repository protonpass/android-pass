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

package proton.android.pass.features.item.details.detail.presentation

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonpresentation.fakes.FakeItemDetailsHandler
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.FakeGetItemActions
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

internal class ItemDetailsViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var getItemById: FakeGetItemById
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var observeShare: FakeObserveShare

    private val shareId = ShareId("share-id")
    private val itemId = ItemId("item-id")

    @Before
    internal fun setUp() {
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = shareId.id
            get()[CommonNavArgId.ItemId.key] = itemId.id
        }
        getItemById = FakeGetItemById()
        telemetryManager = FakeTelemetryManager()
        observeShare = FakeObserveShare()
    }

    @Test
    internal fun `WHEN item is not found THEN telemetry is not sent`() = runTest {
        getItemById.emit(Result.failure(IllegalStateException("Item not found")))

        createViewModel()
        advanceUntilIdle()

        assertThat(telemetryManager.getMemory()).isEmpty()
    }

    private fun createViewModel(): ItemDetailsViewModel = ItemDetailsViewModel(
        savedStateHandleProvider = savedStateHandleProvider,
        getItemActions = FakeGetItemActions(),
        getItemById = getItemById,
        getUserPlan = FakeGetUserPlan(),
        observeShare = observeShare,
        telemetryManager = telemetryManager,
        itemDetailsHandler = FakeItemDetailsHandler()
    )
}
