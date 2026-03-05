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

package proton.android.pass.data.impl.usecases.simplelogin

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.usecases.FakeCreateItem
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.fakes.repositories.FakeSimpleLoginRepository
import proton.android.pass.data.fakes.repositories.FakeUserAccessDataRepository
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.simplelogin.SimpleLoginAlias
import proton.android.pass.domain.simplelogin.SimpleLoginPendingAliases
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.UserAccessDataTestFactory
import proton.android.pass.test.domain.VaultTestFactory

class SyncSimpleLoginPendingAliasesImplTest {

    private lateinit var instance: SyncSimpleLoginPendingAliasesImpl
    private lateinit var repository: FakeSimpleLoginRepository
    private lateinit var createItem: FakeCreateItem
    private lateinit var shareKeyRepository: FakeShareKeyRepository
    private lateinit var userAccessDataRepository: FakeUserAccessDataRepository
    private lateinit var observeVaults: FakeObserveVaults

    @Before
    fun setup() {
        repository = FakeSimpleLoginRepository()
        createItem = FakeCreateItem().apply {
            setPayload(FakeCreateItem.createPayload())
        }
        shareKeyRepository = FakeShareKeyRepository()
        userAccessDataRepository = FakeUserAccessDataRepository()
        observeVaults = FakeObserveVaults()

        instance = SyncSimpleLoginPendingAliasesImpl(
            repository = repository,
            createItem = createItem,
            shareKeyRepository = shareKeyRepository,
            userAccessDataRepository = userAccessDataRepository,
            observeVaults = observeVaults
        )
    }

    @Test
    fun `does nothing when sync is disabled`() = runTest {
        userAccessDataRepository.sendValue(
            UserAccessDataTestFactory.create(
                isSimpleLoginSyncEnabled = false
            )
        )

        instance(
            userId = USER_ID,
            forceRefresh = false
        )

        assertThat(repository.observeSyncStatusInvocations).isEqualTo(0)
        assertThat(repository.enableSyncInvocations).isEmpty()
        assertThat(repository.createPendingAliasesInvocations).isEmpty()
    }

    @Test
    fun `recovers missing default share by selecting owned vault`() = runTest {
        val fallbackVault = VaultTestFactory.create(
            shareId = ShareId("owned-vault-share-id"),
            isOwned = true
        )
        val syncStatus = SimpleLoginSyncStatus(
            isSyncEnabled = true,
            isPreferenceEnabled = true,
            pendingAliasCount = 1,
            defaultVault = fallbackVault,
            canManageAliases = true
        )

        userAccessDataRepository.sendValue(
            UserAccessDataTestFactory.create(
                isSimpleLoginSyncEnabled = true
            )
        )
        observeVaults.sendResult(Result.success(listOf(fallbackVault)))
        shareKeyRepository.emitGetLatestKeyForShare(ShareKeyTestFactory.createPrivate())
        repository.observeSyncStatusResults.add(Result.failure(ShareNotAvailableError()))
        repository.observeSyncStatusResults.add(Result.success(syncStatus))
        repository.pendingAliases = SimpleLoginPendingAliases(
            aliases = listOf(
                SimpleLoginAlias(
                    id = "pending-alias-id",
                    email = "alias@example.com"
                )
            ),
            total = 1,
            lastToken = null
        )

        instance(
            userId = USER_ID,
            forceRefresh = false
        )

        assertThat(repository.enableSyncInvocations).containsExactly(fallbackVault.shareId)
        assertThat(repository.createPendingAliasesInvocations).hasSize(1)
        assertThat(repository.createPendingAliasesInvocations.first().defaultShareId).isEqualTo(fallbackVault.shareId)
        assertThat(repository.observeSyncStatusInvocations).isEqualTo(2)
    }

    @Test
    fun `returns without calling enableSync when no owned vault exists`() = runTest {
        userAccessDataRepository.sendValue(
            UserAccessDataTestFactory.create(
                isSimpleLoginSyncEnabled = true
            )
        )
        observeVaults.sendResult(Result.success(emptyList()))
        repository.observeSyncStatusResults.add(Result.failure(ShareNotAvailableError()))

        instance(
            userId = USER_ID,
            forceRefresh = false
        )

        assertThat(repository.enableSyncInvocations).isEmpty()
        assertThat(repository.createPendingAliasesInvocations).isEmpty()
        assertThat(repository.observeSyncStatusInvocations).isEqualTo(1)
    }

    @Test
    fun `returns gracefully when second sync status fetch fails after recovery`() = runTest {
        val fallbackVault = VaultTestFactory.create(
            shareId = ShareId("owned-vault-share-id"),
            isOwned = true
        )

        userAccessDataRepository.sendValue(
            UserAccessDataTestFactory.create(
                isSimpleLoginSyncEnabled = true
            )
        )
        observeVaults.sendResult(Result.success(listOf(fallbackVault)))
        repository.observeSyncStatusResults.add(Result.failure(ShareNotAvailableError()))
        repository.observeSyncStatusResults.add(Result.failure(ShareNotAvailableError()))

        instance(
            userId = USER_ID,
            forceRefresh = false
        )

        assertThat(repository.enableSyncInvocations).containsExactly(fallbackVault.shareId)
        assertThat(repository.createPendingAliasesInvocations).isEmpty()
        assertThat(repository.observeSyncStatusInvocations).isEqualTo(2)
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
