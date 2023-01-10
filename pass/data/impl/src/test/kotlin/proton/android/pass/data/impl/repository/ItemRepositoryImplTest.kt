package proton.android.pass.data.impl.repository

import kotlinx.coroutines.test.runTest
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.TestCreateItem
import proton.android.pass.crypto.fakes.usecases.TestOpenItem
import proton.android.pass.crypto.fakes.usecases.TestUpdateItem
import proton.android.pass.data.impl.fakes.TestKeyPacketRepository
import proton.android.pass.data.impl.fakes.TestLocalItemDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestRemoteItemDataSource
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.data.impl.fakes.TestVaultKeyRepository
import proton.android.pass.data.impl.generator.TestProtoItemGenerator
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ItemContents
import proton.pass.domain.Share
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestPublicAddressRepository
import proton.android.pass.test.TestUserAddressRepository
import proton.android.pass.test.crypto.TestCryptoContext
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemKey
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestVaultKey
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
