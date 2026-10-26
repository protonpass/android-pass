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

package proton.android.pass.initializer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeClearUserData
import proton.android.pass.data.fakes.usecases.FakeGetReadyAccounts
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeResetAppToDefaults
import proton.android.pass.test.domain.AccountTestFactory

class AccountCleanupHandlerTest {

    private lateinit var syncStatus: FakeItemSyncStatusRepository
    private lateinit var clearUserData: FakeClearUserData
    private lateinit var getReadyAccounts: FakeGetReadyAccounts
    private lateinit var resetApp: FakeResetAppToDefaults
    private var lastUserCleanups = 0
    private var lastUserCleanupResult: Result<Unit> = Result.success(Unit)
    private lateinit var instance: AccountCleanupHandler

    @Before
    fun setup() {
        syncStatus = FakeItemSyncStatusRepository()
        clearUserData = FakeClearUserData()
        getReadyAccounts = FakeGetReadyAccounts()
        resetApp = FakeResetAppToDefaults()
        lastUserCleanups = 0
        lastUserCleanupResult = Result.success(Unit)
        instance = AccountCleanupHandler(
            itemSyncStatusRepository = syncStatus,
            clearUserData = clearUserData,
            getReadyAccounts = getReadyAccounts,
            resetAppToDefaults = resetApp,
            cleanupLastUser = {
                lastUserCleanups++
                lastUserCleanupResult.getOrThrow()
            }
        )
    }

    @Test
    fun `clears sync status and the user data`() = runTest {
        getReadyAccounts.setResult(listOf(AccountTestFactory.create(userId = OTHER_USER_ID)))

        instance(USER_ID)

        assertThat(syncStatus.clearCount).isEqualTo(1)
        assertThat(clearUserData.clearedUserIds).containsExactly(USER_ID)
    }

    @Test
    fun `does not reset the app while ready accounts remain`() = runTest {
        getReadyAccounts.setResult(listOf(AccountTestFactory.create(userId = OTHER_USER_ID)))

        instance(USER_ID)

        assertThat(lastUserCleanups).isEqualTo(0)
        assertThat(resetApp.invocations).isEqualTo(0)
    }

    @Test
    fun `cleans the database and resets the app when no ready accounts remain`() = runTest {
        instance(USER_ID)

        assertThat(lastUserCleanups).isEqualTo(1)
        assertThat(resetApp.invocations).isEqualTo(1)
    }

    @Test
    fun `still resets the app when clearing user data fails`() = runTest {
        clearUserData.setResult(Result.failure(IllegalStateException("clear failed")))

        instance(USER_ID)

        assertThat(syncStatus.clearCount).isEqualTo(1)
        assertThat(resetApp.invocations).isEqualTo(1)
    }

    @Test
    fun `still resets the app when the database cleanup fails`() = runTest {
        lastUserCleanupResult = Result.failure(IllegalStateException("cleanup failed"))

        instance(USER_ID)

        assertThat(lastUserCleanups).isEqualTo(1)
        assertThat(resetApp.invocations).isEqualTo(1)
    }

    private companion object {
        val USER_ID = UserId("cleanup-user")
        val OTHER_USER_ID = UserId("other-user")
    }
}
