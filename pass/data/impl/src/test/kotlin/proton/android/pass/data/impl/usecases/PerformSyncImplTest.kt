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
import org.junit.Test
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.fakes.usecases.sync.FakeCheckFolderForceSync
import javax.inject.Provider

class PerformSyncImplTest {

    @Test
    fun `asks whether a folders repair is owed once the sync has finished`() = runTest {
        val checkFolderForceSync = FakeCheckFolderForceSync()
        val instance = createInstance(checkFolderForceSync = checkFolderForceSync)

        instance.invoke(USER_ID, forceSync = false, syncMode = SyncMode.Background)

        assertThat(checkFolderForceSync.detailedInvocations)
            .containsExactly(FakeCheckFolderForceSync.Invocation(USER_ID, SyncMode.Background))
    }

    @Test
    fun `does not re-enter the check for the repair's own sync`() = runTest {
        val checkFolderForceSync = FakeCheckFolderForceSync()
        val instance = createInstance(checkFolderForceSync = checkFolderForceSync)

        instance.invoke(
            userId = USER_ID,
            forceSync = true,
            syncReason = SyncReason.FolderRepair,
            syncMode = SyncMode.Background
        )

        assertThat(checkFolderForceSync.detailedInvocations).isEmpty()
    }

    private fun createInstance(checkFolderForceSync: FakeCheckFolderForceSync = FakeCheckFolderForceSync()) =
        PerformSyncImpl(
            syncUserEvents = NoOpSyncUserEvents,
            checkFolderForceSync = Provider { checkFolderForceSync }
        )

    private object NoOpSyncUserEvents : SyncUserEvents {
        override suspend fun invoke(
            userId: UserId,
            forceSync: Boolean,
            trigger: String,
            syncReason: SyncReason,
            syncMode: SyncMode
        ) = Unit
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
