package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

class MigrateVaultImplTest {
    @get:Rule
    val dispatcher = MainDispatcherRule()


    private lateinit var accountManager: TestAccountManager
    private lateinit var shareRepository: TestShareRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var migrateVault: MigrateVault

    @Before
    fun setUp() {
        accountManager = TestAccountManager()
        shareRepository = TestShareRepository()
        itemRepository = TestItemRepository()
        migrateVault = MigrateVaultImpl(
            accountManager = accountManager,
            shareRepository = shareRepository,
            itemRepository = itemRepository,
        )
    }

    @Test
    fun `calls migrateItem with the expected contents`() = runTest {
        val userId = UserId("userId")
        val originShare = ShareId("origin")
        val destShare = ShareId("dest")
        val itemId = ItemId("itemId")

        val origin = TestShare.create().copy(id = originShare)
        val destination = TestShare.create().copy(id = destShare)
        val item = TestItem.create().copy(shareId = originShare, id = itemId)

        accountManager.sendPrimaryUserId(userId)
        itemRepository.sendObserveItemList(listOf(item))
        itemRepository.setMigrateItemResult(Result.success(item))
        shareRepository.setGetByIdResult(Result.success(destination))
        migrateVault(originShare, destShare)

        val memory = itemRepository.getMigrateItemMemory()
        val expected = TestItemRepository.MigrateItemPayload(
            userId = userId,
            itemId = itemId,
            source = origin,
            destination = destination
        )
        assertThat(memory.size).isEqualTo(1)

        val memoryItem = memory.first()
        assertThat(memoryItem.itemId).isEqualTo(expected.itemId)
        assertThat(memoryItem.destination).isEqualTo(destination)
        assertThat(memoryItem.userId).isEqualTo(userId)
    }
}
