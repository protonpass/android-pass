package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.TestCreateVault
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.fakes.TestLocalShareDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestReencryptShareContents
import proton.android.pass.data.impl.fakes.TestRemoteShareDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.android.pass.data.impl.repositories.ShareRepositoryImpl
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.test.TestUserAddressRepository
import proton.android.pass.test.TestUserRepository
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.Share
import proton.pass.domain.ShareId

class ShareRepositoryTest {

    private val encryptionContextProvider = TestEncryptionContextProvider()

    private lateinit var instance: ShareRepositoryImpl
    private lateinit var remote: TestRemoteShareDataSource
    private lateinit var local: TestLocalShareDataSource

    @Before
    fun setup() {
        remote = TestRemoteShareDataSource()
        local = TestLocalShareDataSource()

        instance = ShareRepositoryImpl(
            database = TestPassDatabase(),
            userRepository = TestUserRepository(),
            userAddressRepository = TestUserAddressRepository().apply {
                setAddresses(listOf(generateAddress("address1", UserId(USER_ID))))
            },
            remoteShareDataSource = remote,
            localShareDataSource = local,
            reencryptShareContents = TestReencryptShareContents().apply {
                setResponse(Result.success(null))
            },
            createVault = TestCreateVault().apply {
                setResult(Result.success(TestCreateVault.generateOutput()))
            },
            updateVault = proton.android.pass.crypto.fakes.usecases.TestUpdateVault(),
            encryptionContextProvider = encryptionContextProvider,
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf())
            }
        )
    }

    @Test
    fun `refresh shares updates shares`() = runTest {
        // GIVEN
        val userId = UserId(USER_ID)
        // initial state: [share1(primary), share2, share3, share4]
        // desired state: [share1, share2(primary), share3, share5]
        val share1 = TestShare.create(ShareId("123"), isPrimary = true)
        val share2 = TestShare.create(ShareId("456"))
        val share3 = TestShare.create(ShareId("789"))
        val share4 = TestShare.create(ShareId("654"))
        val share5 = TestShare.create(ShareId("321"))

        val asEntities = listOf(
            share1.toEntity(),
            share2.toEntity(),
            share3.toEntity(),
            share4.toEntity()
        )
        val asResponses = listOf(
            share1.copy(isPrimary = false).toResponse(),
            share2.copy(isPrimary = true).toResponse(),
            share3.toResponse(),
            share5.toResponse()
        )

        remote.setGetSharesResponse(Result.success(asResponses))
        local.emitAllSharesForUser(asEntities)

        local.setDeleteSharesResponse(Result.success(true))
        local.setUpsertResponse(Result.success(Unit))
        local.setSetPrimaryShareStatusResult(Result.success(Unit))

        // WHEN
        instance.refreshShares(userId)

        // THEN

        // Primary status should have changed
        val updateStatusMemory = local.getSetPrimaryShareMemory()
        assertThat(updateStatusMemory.size).isEqualTo(2)
        assertThat(updateStatusMemory[0].shareId).isEqualTo(share1.id)
        assertThat(updateStatusMemory[0].isPrimary).isEqualTo(false)
        assertThat(updateStatusMemory[1].shareId).isEqualTo(share2.id)
        assertThat(updateStatusMemory[1].isPrimary).isEqualTo(true)

        // Upsert should have been called with 1 share
        val upsertMemory = local.getUpsertMemory()
        assertThat(upsertMemory.size).isEqualTo(1)
        assertThat(upsertMemory[0]).isEqualTo(listOf(share5.toEntity()))

        // share4 should be deleted
        val deleteMemory = local.getDeleteMemory()
        assertThat(deleteMemory.size).isEqualTo(1)
        assertThat(deleteMemory[0]).isEqualTo(setOf(share4.id))
    }

    private fun Share.toResponse(): ShareResponse = ShareResponse(
        shareId = id.id,
        vaultId = vaultId.id,
        addressId = "addressid-123",
        primary = isPrimary,
        targetType = 1,
        targetId = vaultId.id,
        permission = 1,
        content = null,
        contentKeyRotation = null,
        contentFormatVersion = null,
        expirationTime = null,
        createTime = 0
    )

    private fun Share.toEntity(): ShareEntity = ShareEntity(
        id = id.id,
        userId = USER_ID,
        addressId = "addressid-123",
        vaultId = vaultId.id,
        targetType = 1,
        targetId = vaultId.id,
        permission = 1,
        isPrimary = isPrimary,
        content = null,
        contentKeyRotation = null,
        contentFormatVersion = null,
        expirationTime = null,
        createTime = 0,
        encryptedContent = null,
        isActive = true
    )

    companion object {
        private const val USER_ID = "123456"
    }

}
