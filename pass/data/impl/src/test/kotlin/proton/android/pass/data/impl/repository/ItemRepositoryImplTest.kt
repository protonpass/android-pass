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
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestUserAddressRepository
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.TestCreateItem
import proton.android.pass.crypto.fakes.usecases.TestMigrateItem
import proton.android.pass.crypto.fakes.usecases.TestOpenItem
import proton.android.pass.crypto.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.crypto.FakeGetItemKeys
import proton.android.pass.data.impl.fakes.TestItemKeyRepository
import proton.android.pass.data.impl.fakes.TestLocalItemDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestRemoteItemDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.data.impl.generator.TestProtoItemGenerator
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Share
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestShareKey
import kotlin.test.assertEquals

class ItemRepositoryImplTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var repository: ItemRepositoryImpl
    private lateinit var createItem: TestCreateItem
    private lateinit var openItem: TestOpenItem
    private lateinit var localItemDataSource: TestLocalItemDataSource
    private lateinit var remoteItemDataSource: TestRemoteItemDataSource
    private lateinit var shareKeyRepository: TestShareKeyRepository
    private lateinit var itemKeyRepository: TestItemKeyRepository

    private val userId = UserId("test-123")
    private lateinit var share: Share

    @Before
    fun setUp() {
        createItem = TestCreateItem()
        openItem = TestOpenItem()
        localItemDataSource = TestLocalItemDataSource()
        remoteItemDataSource = TestRemoteItemDataSource()
        shareKeyRepository = TestShareKeyRepository()
        itemKeyRepository = TestItemKeyRepository()

        share = TestShare.random()

        repository = ItemRepositoryImpl(
            database = TestPassDatabase(),
            accountManager = TestAccountManager(),
            userAddressRepository = TestUserAddressRepository().apply {
                setAddresses(listOf(generateAddress("test1", userId)))
            },
            shareRepository = TestShareRepository(),
            createItem = createItem,
            updateItem = TestUpdateItem(),
            localItemDataSource = localItemDataSource,
            remoteItemDataSource = remoteItemDataSource,
            openItem = openItem,
            encryptionContextProvider = TestEncryptionContextProvider(),
            shareKeyRepository = shareKeyRepository,
            migrateItem = TestMigrateItem(),
            getItemKeys = FakeGetItemKeys()
        )
    }

    @Test
    fun `createItem stores into remote and local datasource`() = runTest {
        shareKeyRepository.emitGetLatestKeyForShare(TestShareKey.createPrivate())
        createItem.setPayload(TestCreateItem.createPayload())

        val name = "title"
        val note = "note"
        val protoItem = TestProtoItemGenerator.generate(name, note)
        val item = TestItem.random(content = protoItem.toByteArray())
        remoteItemDataSource.setCreateItemResponse {
            TestRemoteItemDataSource.createItemRevision(item)
        }
        openItem.setOutput(OpenItemOutput(item = item, itemKey = null))

        repository.createItem(userId, share, ItemContents.Note(name, note))

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
