package proton.android.pass.data.impl.repository

import kotlinx.coroutines.test.runTest
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.TestCreateItem
import proton.android.pass.crypto.fakes.usecases.TestOpenItem
import proton.android.pass.crypto.fakes.usecases.TestUpdateItem
import proton.android.pass.data.impl.fakes.TestLocalItemDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestRemoteItemDataSource
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.data.impl.fakes.TestVaultKeyRepository
import proton.android.pass.data.impl.generator.TestProtoItemGenerator
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ItemContents
import proton.pass.domain.Share
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestUserAddressRepository
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemKey
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestShareKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.api.usecases.OpenItemOutput
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
            shareKeyRepository =
        )
    }

    @Test
    fun `createItem stores into remote and local datasource`() = runTest {
        vaultKeyRepository.setLatestVaultItemKey(
            LoadingResult.Success(
                TestShareKey.createPrivate() to TestItemKey.createPrivate()
            )
        )
        createItem.setPayload(TestCreateItem.createPayload())

        val name = "title"
        val note = "note"
        val protoItem = TestProtoItemGenerator.generate(name, note)
        val item = TestItem.random(content = protoItem.toByteArray())
        remoteItemDataSource.setCreateItemResponse {
            LoadingResult.Success(TestRemoteItemDataSource.createItemRevision(item))
        }
        openItem.setOutput(OpenItemOutput(item = item, itemKey = null))

        val res = repository.createItem(userId, share, ItemContents.Note(name, note))
        assertTrue(res is LoadingResult.Success)

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
