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
import proton.android.pass.data.fakes.usecases.FakeGetFreeUserCount
import proton.android.pass.test.domain.UserTestFactory

class CheckFreeUserLimitImplTest {

    private lateinit var getFreeUserCount: FakeGetFreeUserCount
    private lateinit var instance: CheckFreeUserLimitImpl

    @Before
    fun setup() {
        getFreeUserCount = FakeGetFreeUserCount()
        instance = CheckFreeUserLimitImpl(getFreeUserCount)
    }

    @Test
    fun `free user is allowed when there are no free users`() = runTest {
        getFreeUserCount.setCount(0)

        assertThat(instance(UserTestFactory.create(isFree = true))).isFalse()
    }

    @Test
    fun `free user is rejected when a free user already exists`() = runTest {
        getFreeUserCount.setCount(1)

        assertThat(instance(UserTestFactory.create(isFree = true))).isTrue()
    }

    @Test
    fun `paid user is allowed when a free user already exists`() = runTest {
        getFreeUserCount.setCount(1)

        assertThat(instance(UserTestFactory.create(isFree = false))).isFalse()
    }
}
