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
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.fakes.usecases.FakeApplyPendingEvents
import proton.android.pass.data.fakes.usecases.FakeRefreshAliasSlNotes
import proton.android.pass.data.fakes.usecases.FakeRefreshGroupInvites
import proton.android.pass.data.fakes.usecases.FakeRefreshUserInvites
import proton.android.pass.data.fakes.usecases.simplelogin.FakeSyncSimpleLoginPendingAliases
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag

class PerformSyncImplTest {

    @Test
    fun `refreshes alias notes when user events are disabled`() = runTest {
        val refreshAliasSlNotes = FakeRefreshAliasSlNotes()
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_USER_EVENTS_V1, false)
        }
        val instance = createInstance(refreshAliasSlNotes, featureFlags)

        instance.invoke(USER_ID, forceSync = false)

        assertThat(refreshAliasSlNotes.getInvocationMemory()).containsExactly(USER_ID)
    }

    @Test
    fun `does not refresh alias notes when user events are enabled`() = runTest {
        val refreshAliasSlNotes = FakeRefreshAliasSlNotes()
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_USER_EVENTS_V1, true)
        }
        val instance = createInstance(refreshAliasSlNotes, featureFlags)

        instance.invoke(USER_ID, forceSync = false)

        assertThat(refreshAliasSlNotes.getInvocationMemory()).isEmpty()
    }

    private fun createInstance(
        refreshAliasSlNotes: FakeRefreshAliasSlNotes,
        featureFlags: FakeFeatureFlagsPreferenceRepository
    ) = PerformSyncImpl(
        applyPendingEvents = FakeApplyPendingEvents(),
        refreshUserInvites = FakeRefreshUserInvites(),
        refreshGroupInvites = FakeRefreshGroupInvites(),
        refreshAliasSlNotes = refreshAliasSlNotes,
        syncPendingAliases = FakeSyncSimpleLoginPendingAliases(),
        syncUserEvents = NoOpSyncUserEvents,
        featureFlagsPreferencesRepository = featureFlags
    )

    private object NoOpSyncUserEvents : SyncUserEvents {
        override suspend fun invoke(
            userId: UserId,
            forceSync: Boolean,
            trigger: String
        ) = Unit
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
