/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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
import org.junit.Test
import proton.android.pass.data.api.usecases.ClearUserSyncState
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.fakes.FakeUserEventRepository
import proton.android.pass.domain.UserEventId

internal class ClearUserSyncStateImplTest {

    @Test
    fun `clears the user events cursor during local user cleanup`() = runTest {
        val shareRepository = FakeShareRepository().apply {
            setDeleteSharesResult(Result.success(true))
        }
        val userEventRepository = FakeUserEventRepository().apply {
            storeLatestEventId(USER_ID, UserEventId("event-id"))
        }
        val instance: ClearUserSyncState = ClearUserSyncStateImpl(
            shareRepository = shareRepository,
            userEventRepository = userEventRepository
        )

        instance(USER_ID)

        assertThat(shareRepository.deleteLocalSharesForUserMemory()).containsExactly(USER_ID)
        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isEmpty()
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
