/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemdetail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.events.ItemViewed
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem

internal class ItemDetailViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: ItemDetailViewModel
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var getItemById: FakeGetItemById

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        telemetryManager = TestTelemetryManager()
        getItemById = FakeGetItemById()

        instance = ItemDetailViewModel(
            snackbarDispatcher = snackbarDispatcher,
            telemetryManager = telemetryManager,
            getItemById = getItemById,
            userPreferenceRepository = TestPreferenceRepository().apply {
                setUseFaviconsPreference(UseFaviconsPreference.Disabled)
            },
            savedStateHandle = TestSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = SHARE_ID
                get()[CommonNavArgId.ItemId.key] = ITEM_ID
            }
        )
    }

    @Test
    fun `sends the right item data`() = runTest {
        val note = "some text"
        val item = TestItem.create(itemType = ItemType.Note(note, emptyList()))
        getItemById.emit(Result.success(item))

        instance.uiState.test {
            val emitted = awaitItem()
            assertThat(emitted.itemTypeUiState).isEqualTo(ItemTypeUiState.Note)
        }
    }

    @Test
    fun `emits telemetry event when item is read`() = runTest {
        val itemTypes = mapOf(
            ItemTypeUiState.Login to EventItemType.Login,
            ItemTypeUiState.Note to EventItemType.Note,
            ItemTypeUiState.Alias to EventItemType.Alias,
            ItemTypeUiState.Password to EventItemType.Password
        )
        itemTypes.forEach { (itemType, eventItemType) ->
            instance.sendItemReadEvent(itemType)

            val twoLastEvents = telemetryManager.getMemory().takeLast(2)
            assertThat(twoLastEvents.first()).isEqualTo(ItemRead(eventItemType))

            val expected = ItemViewed(ShareId(SHARE_ID), ItemId(ITEM_ID))
            assertThat(twoLastEvents.last()).isEqualTo(expected)
        }

        // If we send an Unknown ItemTypeUiState, TelemetryManager should not receive any call
        val previousMemorySize = telemetryManager.getMemory().size
        instance.sendItemReadEvent(ItemTypeUiState.Unknown)
        assertThat(telemetryManager.getMemory().size).isEqualTo(previousMemorySize)
    }

    @Test
    fun `emits error when cannot retrieve item`() = runTest {
        getItemById.emit(Result.failure(IllegalStateException("test")))
        instance.uiState.test {
            val expected = ItemDetailScreenUiState.Initial.copy(event = ItemDetailScreenEvent.Close)
            assertThat(awaitItem()).isEqualTo(expected)

            val message = snackbarDispatcher.snackbarMessage.first().value()
            assertThat(message).isEqualTo(DetailSnackbarMessages.InitError)
        }
    }

    companion object {
        private const val SHARE_ID = "share_id"
        private const val ITEM_ID = "item_id"
    }
}
