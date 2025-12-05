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

package proton.android.pass.data.impl.repository

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.FakeCreateItem
import proton.android.pass.crypto.fakes.usecases.FakeMigrateItem
import proton.android.pass.crypto.fakes.usecases.FakeOpenItem
import proton.android.pass.crypto.fakes.usecases.FakeUpdateItem
import proton.android.pass.data.fakes.crypto.FakeGetShareAndItemKey
import proton.android.pass.data.impl.fakes.FakeItemKeyRepository
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.FakePassDatabase
import proton.android.pass.data.impl.fakes.FakeRemoteItemDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.generator.TestProtoItemGenerator
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Share
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import kotlin.test.assertEquals

class ItemRepositoryImplTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var repository: ItemRepositoryImpl
    private lateinit var createItem: FakeCreateItem
    private lateinit var openItem: FakeOpenItem
    private lateinit var localItemDataSource: FakeLocalItemDataSource
    private lateinit var remoteItemDataSource: FakeRemoteItemDataSource
    private lateinit var shareKeyRepository: FakeShareKeyRepository
    private lateinit var itemKeyRepository: FakeItemKeyRepository

    private val userId = UserId("test-123")
    private lateinit var share: Share

    @Before
    fun setUp() {
        createItem = FakeCreateItem()
        openItem = FakeOpenItem()
        localItemDataSource = FakeLocalItemDataSource()
        remoteItemDataSource = FakeRemoteItemDataSource()
        shareKeyRepository = FakeShareKeyRepository()
        itemKeyRepository = FakeItemKeyRepository()

        share = ShareTestFactory.random()

        repository = ItemRepositoryImpl(
            database = FakePassDatabase(),
            accountManager = FakeAccountManager(),
            userAddressRepository = FakeUserAddressRepository().apply {
                setAddresses(listOf(generateAddress("test1", userId)))
            },
            shareRepository = FakeShareRepository(),
            createItem = createItem,
            updateItem = FakeUpdateItem(),
            localItemDataSource = localItemDataSource,
            remoteItemDataSource = remoteItemDataSource,
            openItem = openItem,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            shareKeyRepository = shareKeyRepository,
            migrateItem = FakeMigrateItem(),
            getShareAndItemKey = FakeGetShareAndItemKey()
        )
    }

    @Test
    fun `createItem stores into remote and local datasource`() = runTest {
        shareKeyRepository.emitGetLatestKeyForShare(ShareKeyTestFactory.createPrivate())
        createItem.setPayload(FakeCreateItem.createPayload())

        val name = "title"
        val note = "note"
        val protoItem = TestProtoItemGenerator.generate(name, note)
        val item = ItemTestFactory.random(content = protoItem.toByteArray())
        remoteItemDataSource.setCreateItemResponse {
            FakeRemoteItemDataSource.createItemRevision(item)
        }
        openItem.setOutput(OpenItemOutput(item = item, itemKey = null))

        repository.createItem(userId, share, ItemContents.Note(name, note, emptyList()))

        val remoteMemory = remoteItemDataSource.getCreateItemMemory()
        assertEquals(1, remoteMemory.size)

        val remoteItem = remoteMemory.first()
        assertEquals(remoteItem.userId, userId)
        assertEquals(remoteItem.shareId, share.id)

        val localMemory = localItemDataSource.getMemory()
        assertEquals(1, localMemory.size)

        val localItem = localMemory.first()
        assertEquals(localItem.userId, userId.id)
        assertEquals(localItem.aliasEmail, null)
    }

}
