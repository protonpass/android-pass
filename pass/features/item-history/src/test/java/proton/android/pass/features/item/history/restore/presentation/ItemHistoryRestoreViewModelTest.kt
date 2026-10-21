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

package proton.android.pass.features.item.history.restore.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.commonpresentation.fakes.FakeItemDetailsHandler
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveAllItemRevisionAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeRestoreAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeSetAttachmentToBeUnlinked
import proton.android.pass.data.fakes.usecases.items.FakeOpenItemRevision
import proton.android.pass.data.fakes.usecases.items.FakeRestoreItemRevision
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.features.item.history.navigation.ItemHistoryRevisionNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.domain.items.ItemIdTestFactory
import proton.android.pass.test.domain.shares.ShareIdTestFactory

internal class ItemHistoryRestoreViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private val shareId = ShareIdTestFactory.create()
    private val itemId = ItemIdTestFactory.create()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var openItemRevision: FakeOpenItemRevision
    private lateinit var getItemById: FakeGetItemById
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var itemDetailsHandler: FakeItemDetailsHandler

    @Before
    internal fun setUp() {
        savedStateHandleProvider = FakeSavedStateHandleProvider()
        openItemRevision = FakeOpenItemRevision()
        getItemById = FakeGetItemById()
        snackbarDispatcher = FakeSnackbarDispatcher()
        itemDetailsHandler = FakeItemDetailsHandler()

        getItemById.emit(Result.success(ItemTestFactory.createNote()))
        savedStateHandleProvider.get().apply {
            set(CommonNavArgId.ShareId.key, shareId.id)
            set(CommonNavArgId.ItemId.key, itemId.id)
            set(ItemHistoryRevisionNavArgId.key, encodedRevision())
        }
    }

    @Test
    internal fun `WHEN opening item revision fails THEN dispatch open error snackbar`() = runTest {
        openItemRevision.setError(IllegalStateException("Bad AEAD tag"))

        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ItemHistoryRestoreState.Initial)
            cancelAndIgnoreRemainingEvents()
        }
        snackbarDispatcher.snackbarMessage.test {
            val message = awaitItem().value()
            assertThat(message).isEqualTo(ItemHistoryRestoreSnackbarMessage.OpenItemRevisionError)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(snackbarDispatcher.invocationCount).isEqualTo(1)
    }

    @Test
    internal fun `WHEN getting current item fails THEN dispatch open error snackbar once`() = runTest {
        openItemRevision.setItem(ItemTestFactory.createNote())
        getItemById.emit(Result.failure(IllegalStateException("Item not found")))

        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ItemHistoryRestoreState.Initial)
            cancelAndIgnoreRemainingEvents()
        }
        snackbarDispatcher.snackbarMessage.test {
            val message = awaitItem().value()
            assertThat(message).isEqualTo(ItemHistoryRestoreSnackbarMessage.OpenItemRevisionError)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(snackbarDispatcher.invocationCount).isEqualTo(1)
    }

    @Test
    internal fun `WHEN opening item revision succeeds THEN state reaches item details without snackbar`() = runTest {
        val item = ItemTestFactory.createNote()
        openItemRevision.setItem(item)
        itemDetailsHandler.setItemDetailState(createNoteDetailState(item))

        val viewModel = createViewModel()

        viewModel.state.test {
            var state = awaitItem()
            if (state == ItemHistoryRestoreState.Initial) {
                state = awaitItem()
            }
            assertThat(state).isInstanceOf(ItemHistoryRestoreState.ItemDetails::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(openItemRevision.invocationCount).isEqualTo(1)
        assertThat(snackbarDispatcher.invocationCount).isEqualTo(0)
    }

    @Test
    internal fun `WHEN revision is opened before downstream collectors subscribe THEN it is replayed to them`() =
        runTest {
            val item = ItemTestFactory.createNote()
            openItemRevision.setItem(item)
            getItemById.emit(Result.success(item))
            itemDetailsHandler.setItemDetailState(createNoteDetailState(item))

            val viewModel = createViewModel()

            assertThat(viewModel.state.value).isInstanceOf(ItemHistoryRestoreState.ItemDetails::class.java)
            assertThat(openItemRevision.invocationCount).isEqualTo(1)
        }

    private fun createNoteDetailState(item: Item) = ItemDetailState.Note(
        itemContents = ItemContents.Note(
            title = "title",
            note = "note",
            customFields = emptyList()
        ),
        itemId = item.id,
        shareId = item.shareId,
        isItemPinned = false,
        itemShare = ShareTestFactory.random(),
        itemCreatedAt = item.createTime,
        itemModifiedAt = item.modificationTime,
        itemLastAutofillAtOption = None,
        itemRevision = item.revision,
        itemState = ItemState.Active,
        itemDiffs = ItemDiffs.Note(),
        itemShareCount = 0,
        attachmentsState = AttachmentsState(
            draftAttachmentsList = emptyList(),
            attachmentsList = emptyList(),
            loadingAttachments = emptySet(),
            needsUpgrade = None,
            canUpgrade = false
        ),
        customFieldTotps = emptyMap(),
        detailEvent = DetailEvent.Idle,
        breadcrumbs = emptyList()
    )

    private fun encodedRevision(): String {
        val revision = ItemRevision(
            itemId = itemId.id,
            revision = 1,
            contentFormatVersion = 1,
            keyRotation = 1,
            content = "content",
            itemKey = null,
            state = 1,
            aliasEmail = null,
            createTime = 0,
            modifyTime = 0,
            lastUseTime = null,
            revisionTime = 0,
            isPinned = false,
            pinTime = null,
            flags = 0,
            shareCount = 0,
            folderId = "folder-id"
        )
        return NavParamEncoder.encode(Json.encodeToString(revision))
    }

    private fun createViewModel() = ItemHistoryRestoreViewModel(
        savedStateHandleProvider = savedStateHandleProvider,
        openItemRevision = openItemRevision,
        observeAllItemRevisionAttachments = FakeObserveAllItemRevisionAttachments(),
        restoreItemRevision = FakeRestoreItemRevision(),
        restoreAttachments = FakeRestoreAttachments(),
        linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
        setAttachmentToBeUnlinked = FakeSetAttachmentToBeUnlinked(),
        itemDetailsHandler = itemDetailsHandler,
        snackbarDispatcher = snackbarDispatcher,
        encryptionContextProvider = FakeEncryptionContextProvider(),
        getItemById = getItemById
    )

}
