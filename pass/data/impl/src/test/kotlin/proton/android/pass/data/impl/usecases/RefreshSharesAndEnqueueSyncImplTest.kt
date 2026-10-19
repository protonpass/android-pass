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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.fakes.usecases.FakeCanCreateDefaultVault
import proton.android.pass.data.fakes.usecases.FakeCreateVault
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.impl.fakes.FakeDefaultVaultContentProvider
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.fakes.FakeWorkManager
import proton.android.pass.data.impl.usecases.capabilities.DecideDefaultVaultCreationImpl
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.data.api.usecases.RefreshSharesResult as UseCaseRefreshSharesResult

class RefreshSharesAndEnqueueSyncImplTest {

    private lateinit var shareRepository: FakeShareRepository
    private lateinit var createVault: FakeCreateVault
    private lateinit var canCreateDefaultVault: FakeCanCreateDefaultVault
    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var instance: RefreshSharesAndEnqueueSyncImpl

    @Before
    fun setup() {
        shareRepository = FakeShareRepository().apply {
            setRefreshSharesResult(
                RefreshSharesResult(
                    allShareIds = emptySet(),
                    newShareIds = emptySet(),
                    wasFirstSync = false,
                    hasInactiveShares = false,
                    hasInvalidGroupShares = false
                )
            )
        }
        createVault = FakeCreateVault().apply { setResult(Result.success(ShareTestFactory.Vault.create())) }
        canCreateDefaultVault = FakeCanCreateDefaultVault()
        internalSettingsRepository = FakeInternalSettingsRepository()

        instance = RefreshSharesAndEnqueueSyncImpl(
            defaultVaultContentProvider = FakeDefaultVaultContentProvider(),
            shareRepository = shareRepository,
            itemSyncStatusRepository = FakeItemSyncStatusRepository(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            createVault = createVault,
            decideDefaultVaultCreation = DecideDefaultVaultCreationImpl(
                canCreateDefaultVault = canCreateDefaultVault,
                internalSettingsRepository = internalSettingsRepository
            ),
            internalSettingsRepository = internalSettingsRepository,
            workManager = FakeWorkManager()
        )
    }

    @Test
    fun `creates default vault when no shares and default vault was never created`() = runTest {
        val result = refresh()

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesVaultCreated)
        assertThat(createVault.memory()).hasSize(1)
    }

    @Test
    fun `skips default vault when no shares and default vault was already created`() = runTest {
        internalSettingsRepository.setDefaultVaultHasBeenCreated(USER_ID)

        val result = refresh()

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesSkipped)
        assertThat(createVault.memory()).isEmpty()
    }

    @Test
    fun `skips default vault when user cannot create vaults`() = runTest {
        canCreateDefaultVault.setResult(false)

        val result = refresh()

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesSkipped)
        assertThat(createVault.memory()).isEmpty()
    }

    @Test
    fun `force sync recreates default vault when it was already created`() = runTest {
        internalSettingsRepository.setDefaultVaultHasBeenCreated(USER_ID)

        val result = refresh(RefreshSharesAndEnqueueSync.SyncType.FULL)

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesVaultCreated)
        assertThat(createVault.memory()).hasSize(1)
    }

    @Test
    fun `force sync skips default vault when user cannot create vaults`() = runTest {
        internalSettingsRepository.setDefaultVaultHasBeenCreated(USER_ID)
        canCreateDefaultVault.setResult(false)

        val result = refresh(RefreshSharesAndEnqueueSync.SyncType.FULL)

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesSkipped)
        assertThat(createVault.memory()).isEmpty()
    }

    @Test
    fun `background full sync does not recreate default vault when it was already created`() = runTest {
        internalSettingsRepository.setDefaultVaultHasBeenCreated(USER_ID)

        val result = refresh(RefreshSharesAndEnqueueSync.SyncType.FULL_BACKGROUND)

        assertThat(result).isEqualTo(UseCaseRefreshSharesResult.NoSharesSkipped)
        assertThat(createVault.memory()).isEmpty()
    }

    @Test
    fun `force sync does not create default vault when shares exist`() = runTest {
        shareRepository.setRefreshSharesResult(
            RefreshSharesResult(
                allShareIds = setOf(ShareId("share-id")),
                newShareIds = emptySet(),
                wasFirstSync = false,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        internalSettingsRepository.setDefaultVaultHasBeenCreated(USER_ID)

        val result = refresh(RefreshSharesAndEnqueueSync.SyncType.FULL)

        assertThat(result).isInstanceOf(UseCaseRefreshSharesResult.SharesFound::class.java)
        assertThat(createVault.memory()).isEmpty()
    }

    private suspend fun refresh(
        syncType: RefreshSharesAndEnqueueSync.SyncType = RefreshSharesAndEnqueueSync.SyncType.INCREMENTAL
    ): UseCaseRefreshSharesResult = instance(
        userId = USER_ID,
        syncType = syncType,
        workerOrigin = "test",
        syncReason = SyncReason.Default
    )

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
