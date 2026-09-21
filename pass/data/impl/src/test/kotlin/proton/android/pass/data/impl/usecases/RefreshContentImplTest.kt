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
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeRefreshSharesAndEnqueueSync

internal class RefreshContentImplTest {

    private lateinit var accountManager: FakeAccountManager
    private lateinit var itemSyncStatusRepository: FakeItemSyncStatusRepository
    private lateinit var refreshSharesAndEnqueueSync: FakeRefreshSharesAndEnqueueSync
    private lateinit var instance: RefreshContentImpl

    @Before
    fun setup() {
        accountManager = FakeAccountManager().apply { sendPrimaryUserId(USER_ID) }
        itemSyncStatusRepository = FakeItemSyncStatusRepository()
        refreshSharesAndEnqueueSync = FakeRefreshSharesAndEnqueueSync()
        instance = RefreshContentImpl(
            accountManager = accountManager,
            itemSyncStatusRepository = itemSyncStatusRepository,
            refreshSharesAndEnqueueSync = refreshSharesAndEnqueueSync
        )
    }

    @Test
    fun `carries the folder repair reason through a retry`() = runTest {
        itemSyncStatusRepository.setReason(SyncReason.FolderRepair)

        instance(USER_ID)

        assertThat(refreshSharesAndEnqueueSync.invocations).hasSize(1)
        assertThat(refreshSharesAndEnqueueSync.invocations.first().syncReason)
            .isEqualTo(SyncReason.FolderRepair)
    }

    @Test
    fun `retries an ordinary sync without a reason`() = runTest {
        instance(USER_ID)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().syncReason)
            .isEqualTo(SyncReason.Default)
    }

    @Test
    fun `always retries as a full sync`() = runTest {
        instance(USER_ID)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().syncType)
            .isEqualTo(RefreshSharesAndEnqueueSync.SyncType.FULL)
    }

    private companion object {

        private val USER_ID = UserId("user-id")

    }
}
