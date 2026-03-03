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

import android.content.Context
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsSource
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.data.fakes.usecases.FakeGetItemActions
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

internal class ItemDetailsViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var observeItemById: FakeObserveItemById
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
        observeItemById = FakeObserveItemById()
        telemetryManager = FakeTelemetryManager()
        observeShare = FakeObserveShare()
    }

    @Test
    internal fun `WHEN observed item is not available THEN telemetry is not sent`() = runTest {
        observeItemById.emitValue(Result.failure(IllegalStateException("Item not found")))

        createViewModel()
        advanceUntilIdle()

        assertThat(telemetryManager.getMemory()).isEmpty()
    }

    private fun createViewModel(): ItemDetailsViewModel = ItemDetailsViewModel(
        savedStateHandleProvider = savedStateHandleProvider,
        getItemActions = FakeGetItemActions(),
        getUserPlan = FakeGetUserPlan(),
        observeItemById = observeItemById,
        observeShare = observeShare,
        telemetryManager = telemetryManager,
        itemDetailsHandler = FakeItemDetailsHandler()
    )

    private class FakeItemDetailsHandler : ItemDetailsHandler {
        override fun observeItemDetails(
            item: Item,
            source: ItemDetailsSource,
            savedStateEntries: Map<String, Any?>
        ): Flow<ItemDetailState> = emptyFlow()

        override suspend fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) = Unit

        override suspend fun onItemDetailsFieldClicked(fieldType: ItemDetailsFieldType) = Unit

        override fun updateItemDetailsContent(
            revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>,
            itemCategory: proton.android.pass.domain.items.ItemCategory,
            itemContents: ItemContents
        ): ItemContents = itemContents

        override fun updateItemDetailsDiffs(
            itemCategory: proton.android.pass.domain.items.ItemCategory,
            baseItemContents: ItemContents,
            otherItemContents: ItemContents,
            baseAttachments: List<Attachment>,
            otherAttachments: List<Attachment>
        ): ItemDiffs = ItemDiffs.None

        override fun consumeEvent(event: DetailEvent) = Unit
    }
}
