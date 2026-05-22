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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.FakeCreateItem
import proton.android.pass.crypto.fakes.usecases.FakeMigrateItem
import proton.android.pass.crypto.fakes.usecases.FakeOpenItem
import proton.android.pass.crypto.fakes.usecases.FakeUpdateItem
import proton.android.pass.data.fakes.crypto.FakeGetShareAndItemKey
import proton.android.pass.data.fakes.repositories.FakeSearchIndexRepository
import proton.android.pass.data.impl.fakes.FakeFolderKeyRepository
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.FakePassDatabase
import proton.android.pass.data.impl.fakes.FakeRemoteItemDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.events.EventToken
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.ShareTestFactory

class ItemRepositoryImplRefreshItemsTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var repository: ItemRepositoryImpl
    private lateinit var openItem: FakeOpenItem
    private lateinit var localItemDataSource: FakeLocalItemDataSource
    private lateinit var remoteItemDataSource: FakeRemoteItemDataSource
    private lateinit var shareKeyRepository: FakeShareKeyRepository
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var userAddressRepository: FakeUserAddressRepository

    private val userId = UserId("test-123")
    private lateinit var share: proton.android.pass.domain.Share

    @Before
    fun setUp() {
        openItem = FakeOpenItem()
        localItemDataSource = FakeLocalItemDataSource()
        remoteItemDataSource = FakeRemoteItemDataSource()
        shareKeyRepository = FakeShareKeyRepository()
        shareRepository = FakeShareRepository()
        userAddressRepository = FakeUserAddressRepository()

        share = ShareTestFactory.random()
        val userAddress = userAddressRepository.generateAddress("test1", userId)
        shareRepository.setGetByIdResult(Result.success(share))
        shareRepository.setGetAddressForShareIdResult(Result.success(userAddress))
        shareKeyRepository.emitGetShareKeys(listOf(ShareKeyTestFactory.createPrivate()))

        repository = ItemRepositoryImpl(
            database = FakePassDatabase(),
            accountManager = FakeAccountManager(),
            userAddressRepository = userAddressRepository.apply { setAddresses(listOf(userAddress)) },
            shareRepository = shareRepository,
            createItem = FakeCreateItem(),
            updateItem = FakeUpdateItem(),
            localItemDataSource = localItemDataSource,
            remoteItemDataSource = remoteItemDataSource,
            openItem = openItem,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            shareKeyRepository = shareKeyRepository,
            migrateItem = FakeMigrateItem(),
            getShareAndItemKey = FakeGetShareAndItemKey(),
            folderKeyRepository = FakeFolderKeyRepository(),
            appDispatchers = FakeAppDispatchers(),
            searchIndexRepository = FakeSearchIndexRepository()
        )
    }

    @Test
    fun `refreshItems writes all successfully fetched items to database in one batch`() = runTest {
        val item1 = ItemTestFactory.random()
        val item2 = ItemTestFactory.random()
        remoteItemDataSource.addGetItemResponse { FakeRemoteItemDataSource.createItemRevision(item1) }
        remoteItemDataSource.addGetItemResponse { FakeRemoteItemDataSource.createItemRevision(item2) }
        openItem.setOutput(OpenItemOutput(item = item1, itemKey = null))

        repository.refreshItems(
            userId,
            listOf(
                SyncEventShareItem(share.id, ItemId("item-1"), EventToken("t")),
                SyncEventShareItem(share.id, ItemId("item-2"), EventToken("t"))
            )
        )

        assertThat(localItemDataSource.getMemory()).hasSize(2)
    }

    @Test
    fun `refreshItems skips items that fail to fetch and still writes the rest`() = runTest {
        val item = ItemTestFactory.random()
        remoteItemDataSource.addGetItemResponse { throw IllegalStateException("network error") }
        remoteItemDataSource.addGetItemResponse { FakeRemoteItemDataSource.createItemRevision(item) }
        openItem.setOutput(OpenItemOutput(item = item, itemKey = null))

        repository.refreshItems(
            userId,
            listOf(
                SyncEventShareItem(share.id, ItemId("item-fail"), EventToken("t")),
                SyncEventShareItem(share.id, ItemId("item-ok"), EventToken("t"))
            )
        )

        assertThat(remoteItemDataSource.getGetItemCallCount()).isEqualTo(2)
        assertThat(localItemDataSource.getMemory()).hasSize(1)
    }

    @Test
    fun `refreshItems does nothing when all items fail to fetch`() = runTest {
        remoteItemDataSource.addGetItemResponse { throw IllegalStateException("network error") }

        repository.refreshItems(
            userId,
            listOf(SyncEventShareItem(share.id, ItemId("item-1"), EventToken("t")))
        )

        assertThat(localItemDataSource.getMemory()).isEmpty()
    }
}
