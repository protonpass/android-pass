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

package proton.android.pass.data.impl.usecases.items

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.usecases.FakeOpenItem
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.impl.fakes.FakeFolderKeyRepository
import proton.android.pass.data.impl.fakes.FakeRemoteItemDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.Item
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.ShareTestFactory

internal class OpenItemRevisionImplTest {

    private lateinit var shareRepository: FakeShareRepository
    private lateinit var shareKeyRepository: FakeShareKeyRepository
    private lateinit var folderKeyRepository: FakeFolderKeyRepository
    private lateinit var openItem: FakeOpenItem
    private lateinit var item: Item

    private lateinit var instance: OpenItemRevisionImpl

    @Before
    fun setUp() {
        val share = ShareTestFactory.random()
        item = ItemTestFactory.createNote()
        shareRepository = FakeShareRepository().apply {
            setGetByIdResult(Result.success(share))
            setGetAddressForShareIdResult(
                Result.success(FakeUserAddressRepository().generateAddress("test"))
            )
        }
        shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetShareKeys(listOf(ShareKeyTestFactory.createPrivate()))
        }
        folderKeyRepository = FakeFolderKeyRepository()
        openItem = FakeOpenItem().apply {
            setOutput(OpenItemOutput(item = item, itemKey = null))
        }

        instance = OpenItemRevisionImpl(
            getPrimaryUserId = FakeGetPrimaryUserId(),
            shareRepository = shareRepository,
            shareKeyRepository = shareKeyRepository,
            folderKeyRepository = folderKeyRepository,
            openItem = openItem
        )
    }

    @Test
    fun `passes the folder key to openItem when the revision has a folder id`() = runTest {
        val folderKey = FolderKey(
            rotation = 1L,
            key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
            responseKey = "folder-key"
        )
        folderKeyRepository.setGetFolderKeyResult(Result.success(folderKey))
        val revision = FakeRemoteItemDataSource.createItemRevision(item).copy(folderId = "folder-id")

        val result = instance(item.shareId, revision, FakeEncryptionContext)

        assertThat(result).isEqualTo(item)
        assertThat(openItem.folderKeyMemory()).containsExactly(folderKey)
    }

    @Test
    fun `passes no folder key to openItem when the revision has no folder id`() = runTest {
        folderKeyRepository.setGetFolderKeyResult(
            Result.success(
                FolderKey(
                    rotation = 1L,
                    key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
                    responseKey = "folder-key"
                )
            )
        )
        val revision = FakeRemoteItemDataSource.createItemRevision(item).copy(folderId = null)

        instance(item.shareId, revision, FakeEncryptionContext)

        assertThat(openItem.folderKeyMemory()).containsExactly(null as FolderKey?)
    }
}
