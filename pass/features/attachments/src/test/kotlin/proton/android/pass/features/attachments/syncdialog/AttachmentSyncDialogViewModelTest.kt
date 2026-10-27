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

package proton.android.pass.features.attachments.syncdialog

import android.content.ContextWrapper
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.FakeObservePrimaryUserId
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.fakes.usecases.attachments.FakeAttachmentDownloadScheduler
import proton.android.pass.data.fakes.usecases.attachments.FakeGetVaultUsage
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveActiveAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveOfflineEnabledShareIds
import proton.android.pass.data.fakes.usecases.attachments.FakeRefreshAttachmentsForItems
import proton.android.pass.data.fakes.usecases.attachments.FakeUpdateAttachmentDownloadStatus
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.UserId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.features.attachments.syncdialog.AttachmentSyncDialogState.SyncStatus
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.network.fakes.FakeNetworkMonitor
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

class AttachmentSyncDialogViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val fakeObservePrimaryUserId = FakeObservePrimaryUserId()
    private val fakeGetPrimaryUserId = FakeGetPrimaryUserId()
    private val fakeObserveOfflineEnabledShareIds = FakeObserveOfflineEnabledShareIds()
    private val fakeObserveVaults = FakeObserveVaults()
    private val fakeObserveItems = FakeObserveItems()
    private val fakeNetworkMonitor = FakeNetworkMonitor()
    private val fakeObserveActiveAttachments = FakeObserveActiveAttachments()
    private val fakeUpdateAttachmentDownloadStatus = FakeUpdateAttachmentDownloadStatus()
    private val fakeRefreshAttachmentsForItems = FakeRefreshAttachmentsForItems()
    private val fakeEncryptionContextProvider = FakeEncryptionContextProvider()
    private val fakeDownloadScheduler = FakeAttachmentDownloadScheduler()
    private val fakeUserPreferencesRepository = FakePreferenceRepository()
    private val fakeGetVaultUsage = FakeGetVaultUsage()

    @Before
    fun setUp() {
        val items = listOf(ItemTestFactory.createLogin(shareId = SHARE_ID, itemId = ITEM_ID))
        val shareSelection = ShareSelection.Shares(listOf(SHARE_ID))

        fakeObservePrimaryUserId.emitUserId(USER_ID)
        fakeGetPrimaryUserId.setResult(USER_ID)
        fakeObserveOfflineEnabledShareIds.emit(listOf(SHARE_ID))
        fakeObserveVaults.sendResult(Result.success(listOf(OFFLINE_VAULT)))
        fakeObserveItems.emit(
            FakeObserveItems.Params(selection = shareSelection, userId = USER_ID),
            items
        )
        fakeObserveItems.emit(
            FakeObserveItems.Params(
                selection = shareSelection,
                userId = USER_ID,
                itemFlags = mapOf(ItemFlag.HasAttachments to true)
            ),
            items
        )
    }

    @Test
    fun `status stays Loading while refresh is in flight even when all attachments are downloaded`() = runTest {
        fakeRefreshAttachmentsForItems.holdUntilReleased()
        fakeObserveActiveAttachments.emit(listOf(attachment(AttachmentDownloadStatus.Downloaded)))
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(fakeRefreshAttachmentsForItems.invocationCount).isEqualTo(1)
            assertThat(expectMostRecentItem().status).isEqualTo(SyncStatus.Loading)

            fakeRefreshAttachmentsForItems.release()

            val finalState = expectMostRecentItem()
            assertThat(finalState.status).isEqualTo(SyncStatus.Done)
            assertThat(finalState.downloaded).isEqualTo(1)
            assertThat(finalState.total).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `status stays Loading while refresh is in flight with no attachments`() = runTest {
        fakeRefreshAttachmentsForItems.holdUntilReleased()
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(fakeRefreshAttachmentsForItems.invocationCount).isEqualTo(1)
            assertThat(expectMostRecentItem().status).isEqualTo(SyncStatus.Loading)

            fakeRefreshAttachmentsForItems.release()

            assertThat(expectMostRecentItem().status).isEqualTo(SyncStatus.Done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure completes refresh without leaving status Loading`() = runTest {
        fakeRefreshAttachmentsForItems.setResult(Result.failure(RuntimeException("refresh failed")))
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(fakeRefreshAttachmentsForItems.invocationCount).isEqualTo(1)
            assertThat(expectMostRecentItem().status).isEqualTo(SyncStatus.PartialFailure)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `going offline while refresh is in flight shows WaitingForConnection`() = runTest {
        fakeRefreshAttachmentsForItems.holdUntilReleased()
        fakeObserveActiveAttachments.emit(listOf(attachment(AttachmentDownloadStatus.Pending)))
        val viewModel = createViewModel()

        viewModel.state.test {
            fakeNetworkMonitor.emit(NetworkStatus.Offline)
            assertThat(awaitStatus(SyncStatus.WaitingForConnection).status).isEqualTo(SyncStatus.WaitingForConnection)

            fakeRefreshAttachmentsForItems.release()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloading attachment while refresh is in flight shows Downloading`() = runTest {
        fakeRefreshAttachmentsForItems.holdUntilReleased()
        fakeObserveActiveAttachments.emit(listOf(attachment(AttachmentDownloadStatus.Downloading)))
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(fakeRefreshAttachmentsForItems.invocationCount).isEqualTo(1)
            assertThat(awaitStatus(SyncStatus.Downloading).status).isEqualTo(SyncStatus.Downloading)

            fakeRefreshAttachmentsForItems.release()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry schedules the attachment for its owner when the primary user is another account`() = runTest {
        fakeGetPrimaryUserId.setResult(OTHER_USER_ID)
        val viewModel = createViewModel()

        viewModel.onRetryAttachment(attachment(AttachmentDownloadStatus.Failed))

        assertThat(fakeDownloadScheduler.scheduledAttachments).containsExactly(
            FakeAttachmentDownloadScheduler.ScheduledAttachment(
                userId = USER_ID,
                shareId = SHARE_ID,
                itemId = ITEM_ID,
                attachmentId = ATTACHMENT_ID
            )
        )
    }

    private suspend fun ReceiveTurbine<AttachmentSyncDialogState>.awaitStatus(
        status: SyncStatus
    ): AttachmentSyncDialogState {
        var item = awaitItem()
        while (item.status != status) {
            item = awaitItem()
        }
        return item
    }

    private fun createViewModel() = AttachmentSyncDialogViewModel(
        context = object : ContextWrapper(null) {},
        observePrimaryUserId = fakeObservePrimaryUserId,
        getPrimaryUserId = fakeGetPrimaryUserId,
        observeOfflineEnabledShareIds = fakeObserveOfflineEnabledShareIds,
        observeVaults = fakeObserveVaults,
        observeItems = fakeObserveItems,
        networkMonitor = fakeNetworkMonitor,
        observeActiveAttachments = fakeObserveActiveAttachments,
        updateAttachmentDownloadStatus = fakeUpdateAttachmentDownloadStatus,
        refreshAttachmentsForItems = fakeRefreshAttachmentsForItems,
        encryptionContextProvider = fakeEncryptionContextProvider,
        downloadScheduler = fakeDownloadScheduler,
        userPreferencesRepository = fakeUserPreferencesRepository,
        getVaultUsage = fakeGetVaultUsage
    )

    private fun attachment(status: AttachmentDownloadStatus) = Attachment(
        userId = USER_ID,
        id = ATTACHMENT_ID,
        persistentId = PersistentAttachmentId("persistent-attachment-id"),
        shareId = SHARE_ID,
        itemId = ITEM_ID,
        name = "file.txt",
        mimeType = "text/plain",
        type = AttachmentType.Text,
        size = 10L,
        createTime = Instant.fromEpochMilliseconds(0),
        modifyTime = Instant.fromEpochMilliseconds(0),
        revisionAdded = 1,
        revisionRemoved = null,
        reencryptedKey = EncryptedByteArray(ByteArray(0)),
        chunks = emptyList(),
        encryptionVersion = 1,
        downloadStatus = status
    )

    private companion object {
        val USER_ID = UserId("user-id")
        val OTHER_USER_ID = UserId("other-user-id")
        val SHARE_ID = ShareId("share-id")
        val ITEM_ID = ItemId("item-id")
        val ATTACHMENT_ID = AttachmentId("attachment-id")
        val OFFLINE_VAULT = VaultTestFactory.create(shareId = SHARE_ID).copy(offlineAttachments = true)
    }
}
