package proton.android.pass.data.impl.usecases

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.ShareId

class MigrateVaultImplTest {
    @get:Rule
    val dispatcher = MainDispatcherRule()


    private lateinit var accountManager: TestAccountManager
    private lateinit var shareRepository: TestShareRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider
    private lateinit var migrateVault: MigrateVault

    @Before
    fun setUp() {
        accountManager = TestAccountManager()
        shareRepository = TestShareRepository()
        itemRepository = TestItemRepository()
        encryptionContextProvider = TestEncryptionContextProvider()
        migrateVault = MigrateVaultImpl(
            accountManager = accountManager,
            shareRepository = shareRepository,
            itemRepository = itemRepository,
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `given no elements should delete vault`() = runTest {
        val originShare = ShareId("origin")
        val destShare = ShareId("dest")
        accountManager.sendPrimaryUserId(UserId("userId"))
        itemRepository.sendObserveItemList(emptyList())
        shareRepository.setGetByIdResult(Result.success(TestShare.create()))
        shareRepository.setDeleteVaultResult(Result.success(Unit))
        migrateVault(originShare, destShare)

        val deleteVaultMemory = shareRepository.deleteVaultMemory()
        assertEquals(listOf(originShare), deleteVaultMemory)
    }
}
