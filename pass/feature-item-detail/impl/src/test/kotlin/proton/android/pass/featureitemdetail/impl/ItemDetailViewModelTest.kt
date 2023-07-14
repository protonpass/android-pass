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

package proton.android.pass.featureitemdetail.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.data.fakes.usecases.TestGetItemById
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class ItemDetailViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: ItemDetailViewModel
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var clock: FixedClock
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var getItemById: TestGetItemById

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        clock = FixedClock(Instant.fromEpochSeconds(TEST_TIMESTAMP))
        telemetryManager = TestTelemetryManager()
        getItemById = TestGetItemById()

        instance = ItemDetailViewModel(
            snackbarDispatcher = snackbarDispatcher,
            clock = clock,
            telemetryManager = telemetryManager,
            getItemById = getItemById,
            userPreferenceRepository = TestPreferenceRepository().apply {
                setUseFaviconsPreference(UseFaviconsPreference.Disabled)
            },
            savedStateHandle = TestSavedStateHandleProvider().apply {
                get().set(CommonNavArgId.ShareId.key, SHARE_ID)
                get().set(CommonNavArgId.ItemId.key, ITEM_ID)
            }
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.uiState.test {
            assertThat(awaitItem()).isEqualTo(ItemDetailScreenUiState.Initial)
        }
    }

    @Test
    fun `sends the right item data`() = runTest {
        val note = "some text"
        val item = TestItem.create(itemType = ItemType.Note(note))
        getItemById.emitValue(Result.success(item))

        instance.uiState.test {
            val emitted = awaitItem()
            assertThat(emitted.itemTypeUiState).isEqualTo(ItemTypeUiState.Note)
            assertThat(emitted.moreInfoUiState.createdTime).isEqualTo(item.createTime)
            assertThat(emitted.moreInfoUiState.lastAutofilled).isEqualTo(item.lastAutofillTime)
            assertThat(emitted.moreInfoUiState.lastModified).isEqualTo(item.modificationTime)
            assertThat(emitted.moreInfoUiState.now).isEqualTo(clock.now())
        }

        val memory = getItemById.memory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0]).isEqualTo(TestGetItemById.Payload(ShareId(SHARE_ID), ItemId(ITEM_ID)))
    }

    @Test
    fun `emits telemetry event when item is read`() = runTest {
        val itemTypes = mapOf(
            ItemTypeUiState.Login to EventItemType.Login,
            ItemTypeUiState.Note to EventItemType.Note,
            ItemTypeUiState.Alias to EventItemType.Alias,
            ItemTypeUiState.Password to EventItemType.Password,
        )
        itemTypes.forEach { (itemType, eventItemType) ->
            instance.sendItemReadEvent(itemType)
            assertThat(telemetryManager.getMemory().last()).isEqualTo(ItemRead(eventItemType))
        }

        // If we send an Unknown ItemTypeUiState, TelemetryManager should not receive any call
        val previousMemorySize = telemetryManager.getMemory().size
        instance.sendItemReadEvent(ItemTypeUiState.Unknown)
        assertThat(telemetryManager.getMemory().size).isEqualTo(previousMemorySize)
    }

    @Test
    fun `emits error when cannot retrieve item`() = runTest {
        getItemById.emitValue(Result.failure(IllegalStateException("test")))
        instance.uiState.test {
            assertThat(awaitItem()).isEqualTo(ItemDetailScreenUiState.Initial)

            val message = snackbarDispatcher.snackbarMessage.first().value()
            assertThat(message).isEqualTo(DetailSnackbarMessages.InitError)
        }
    }

    companion object {
        @Suppress("UnderscoresInNumericLiterals")
        private const val TEST_TIMESTAMP = 1681476769L

        private const val SHARE_ID = "share_id"
        private const val ITEM_ID = "item_id"
    }
}
