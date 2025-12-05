/*
 * Copyright (c) 2024 Proton AG
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.fakes.repositories.FakeUserAccessDataRepository
import proton.android.pass.domain.UserAccessData

class ObserveAppNeedsUpdateImplTest {

    private lateinit var instance: ObserveAppNeedsUpdateImpl

    private lateinit var accountManager: FakeAccountManager
    private lateinit var userAccessDataRepository: FakeUserAccessDataRepository

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        userAccessDataRepository = FakeUserAccessDataRepository()
        instance = ObserveAppNeedsUpdateImpl(
            accountManager = accountManager,
            userAccessDataRepository = userAccessDataRepository
        )
    }

    @Test
    fun `emits false if there is no primary user id`() = runTest {
        sendUserId(false)

        val result = instance().first()
        assertThat(result).isFalse()
    }

    @Test
    fun `emits false if there is primary user id but no userPlan`() = runTest {
        sendUserId(true)
        sendUserAccessData(null)

        val result = instance().first()
        assertThat(result).isFalse()
    }

    @Test
    fun `emits false if there is primary user id and userPlan but needsUpdate false`() = runTest {
        sendUserId(true)
        sendUserAccessData(false)

        val result = instance().first()
        assertThat(result).isFalse()
    }

    @Test
    fun `emits true if there is primary user id and userPlan and needsUpdate`() = runTest {
        sendUserId(true)
        sendUserAccessData(true)

        val result = instance().first()
        assertThat(result).isTrue()
    }

    private fun sendUserId(sendValue: Boolean) {
        val value = if (sendValue) UserId("user-id") else null
        accountManager.sendPrimaryUserId(value)
    }

    private fun sendUserAccessData(needsUpdate: Boolean?) {
        val value = needsUpdate?.let {
            UserAccessData(
                pendingInvites = 0,
                waitingNewUserInvites = 0,
                needsUpdate = it,
                protonMonitorEnabled = false,
                aliasMonitorEnabled = false,
                minVersionUpgrade = null,
                isSimpleLoginSyncEnabled = false,
                simpleLoginSyncPendingAliasCount = 0,
                simpleLoginSyncDefaultShareId = "",
                canManageSimpleLoginAliases = false,
                storageAllowed = false,
                storageQuota = 100,
                storageUsed = 1,
                storageMaxFileSize = 1_048_576
            )
        }
        userAccessDataRepository.sendValue(value)
    }

}
