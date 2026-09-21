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

package proton.android.pass.data.impl.usecases.sync

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.ForceSyncFolderPreference

internal class MarkFolderForceSyncCompletedImplTest {

    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var instance: MarkFolderForceSyncCompletedImpl

    @Before
    fun setup() {
        internalSettingsRepository = FakeInternalSettingsRepository()
        instance = MarkFolderForceSyncCompletedImpl(internalSettingsRepository)
    }

    @Test
    fun `settles the repair`() = runTest {
        instance(USER_ID)

        assertThat(internalSettingsRepository.getForceSyncFolderPreference(USER_ID).done).isTrue()
    }

    @Test
    fun `keeps the attempt history`() = runTest {
        internalSettingsRepository.updateForceSyncFolderPreference(USER_ID) {
            ForceSyncFolderPreference.Initial.copy(attempts = 2, lastAttemptAtMs = 1_000L)
        }

        instance(USER_ID)

        val preference = internalSettingsRepository.getForceSyncFolderPreference(USER_ID)
        assertThat(preference.done).isTrue()
        assertThat(preference.attempts).isEqualTo(2)
        assertThat(preference.lastAttemptAtMs).isEqualTo(1_000L)
    }

    @Test
    fun `settles only the given user`() = runTest {
        instance(USER_ID)

        assertThat(internalSettingsRepository.getForceSyncFolderPreference(OTHER_USER_ID).done).isFalse()
    }

    private companion object {

        private val USER_ID = UserId("user-id")
        private val OTHER_USER_ID = UserId("other-user-id")

    }
}
