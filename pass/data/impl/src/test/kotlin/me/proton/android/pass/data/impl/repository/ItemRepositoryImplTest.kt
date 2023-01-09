package me.proton.android.pass.data.impl.repository

import kotlinx.coroutines.test.runTest
import me.proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import me.proton.android.pass.crypto.fakes.usecases.TestCreateItem
import me.proton.android.pass.crypto.fakes.usecases.TestOpenItem
import me.proton.android.pass.crypto.fakes.usecases.TestUpdateItem
import me.proton.android.pass.data.impl.fakes.TestKeyPacketRepository
import me.proton.android.pass.data.impl.fakes.TestLocalItemDataSource
import me.proton.android.pass.data.impl.fakes.TestPassDatabase
import me.proton.android.pass.data.impl.fakes.TestRemoteItemDataSource
import me.proton.android.pass.data.impl.fakes.TestShareRepository
import me.proton.android.pass.data.impl.fakes.TestVaultKeyRepository
import me.proton.android.pass.data.impl.generator.TestProtoItemGenerator
import me.proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.Share
import me.proton.pass.test.MainDispatcherRule
import me.proton.pass.test.TestAccountManager
import me.proton.pass.test.TestPublicAddressRepository
import me.proton.pass.test.TestUserAddressRepository
import me.proton.pass.test.crypto.TestCryptoContext
import me.proton.pass.test.domain.TestItem
import me.proton.pass.test.domain.TestItemKey
import me.proton.pass.test.domain.TestShare
import me.proton.pass.test.domain.TestVaultKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemRepositoryImplTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var repository: ItemRepositoryImpl
    private lateinit var createItem: TestCreateItem
    private lateinit var openItem: TestOpenItem
    private lateinit var localItemDataSource: TestLocalItemDataSource
    private lateinit var remoteItemDataSource: TestRemoteItemDataSource
    private lateinit var vaultKeyRepository: TestVaultKeyRepository

    private val userId = UserId("test-123")
    private lateinit var share: Share

    @Before
    fun setUp() {
        createItem = TestCreateItem()
        openItem = TestOpenItem()
        localItemDataSource = TestLocalItemDataSource()
        remoteItemDataSource = TestRemoteItemDataSource()
        vaultKeyRepository = TestVaultKeyRepository()

        share = TestShare.create()

        repository = ItemRepositoryImpl(
            database = TestPassDatabase(),
            cryptoContext = TestCryptoContext,
            accountManager = TestAccountManager(),
            userAddressRepository = TestUserAddressRepository().apply {
                setAddresses(listOf(generateAddress("test1", userId)))
            },
            keyRepository = TestPublicAddressRepository(),
            vaultKeyRepository = vaultKeyRepository,
            shareRepository = TestShareRepository(),
            createItem = createItem,
            updateItem = TestUpdateItem(),
            localItemDataSource = localItemDataSource,
            remoteItemDataSource = remoteItemDataSource,
            keyPacketRepository = TestKeyPacketRepository(),
            openItem = openItem,
            encryptionContextProvider = TestEncryptionContextProvider()
        )
    }

    @Test
    fun `createItem stores into remote and local datasource`() = runTest {
        vaultKeyRepository.setLatestVaultItemKey(
            Result.Success(
                TestVaultKey.createPrivate() to TestItemKey.createPrivate()
            )
        )
        createItem.setRequest(TestCreateItem.createRequest())

        val name = "title"
        val note = "note"
        val protoItem = TestProtoItemGenerator.generate(name, note)
        val item = TestItem.random(content = protoItem.toByteArray())
        remoteItemDataSource.setCreateItemResponse {
            Result.Success(TestRemoteItemDataSource.createItemRevision(item))
        }
        openItem.setItem(item)

        val res = repository.createItem(userId, share, ItemContents.Note(name, note))
        assertTrue(res is Result.Success)

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
