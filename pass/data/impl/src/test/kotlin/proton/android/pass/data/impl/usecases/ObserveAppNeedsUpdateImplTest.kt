/*
 * Copyright (c) 2024-2026 Proton AG
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
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.repositories.FakeUserAccessDataRepository
import proton.android.pass.data.fakes.usecases.FakeObservePrimaryUserId
import proton.android.pass.domain.UserId
import proton.android.pass.test.domain.UserAccessDataTestFactory

class ObserveAppNeedsUpdateImplTest {

    private lateinit var instance: ObserveAppNeedsUpdateImpl

    private lateinit var observePrimaryUserId: FakeObservePrimaryUserId
    private lateinit var userAccessDataRepository: FakeUserAccessDataRepository

    @Before
    fun setup() {
        observePrimaryUserId = FakeObservePrimaryUserId()
        userAccessDataRepository = FakeUserAccessDataRepository()
        instance = ObserveAppNeedsUpdateImpl(
            observePrimaryUserId = observePrimaryUserId,
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
        observePrimaryUserId.emitUserId(value)
    }

    private fun sendUserAccessData(needsUpdate: Boolean?) {
        val value = needsUpdate?.let { UserAccessDataTestFactory.create(needsUpdate = it) }
        userAccessDataRepository.sendValue(value)
    }

}
