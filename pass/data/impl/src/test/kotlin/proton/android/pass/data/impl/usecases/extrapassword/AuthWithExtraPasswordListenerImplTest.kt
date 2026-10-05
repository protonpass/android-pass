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

package proton.android.pass.data.impl.usecases.extrapassword

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult

class AuthWithExtraPasswordListenerImplTest {

    private lateinit var instance: AuthWithExtraPasswordListenerImpl

    @Before
    fun setup() {
        instance = AuthWithExtraPasswordListenerImpl()
    }

    @Test
    fun waitsUntilResultIsSetForUser() = runTest {
        val pending = async { instance.onAuthWithExtraPassword(USER_ID) }
        runCurrent()
        assertThat(pending.isActive).isTrue()

        instance.setState(USER_ID, AuthWithExtraPasswordResult.Success)

        assertThat(pending.await()).isEqualTo(AuthWithExtraPasswordResult.Success)
    }

    @Test
    fun returnsResultSetBeforeWaiting() = runTest {
        instance.setState(USER_ID, AuthWithExtraPasswordResult.Failure)

        assertThat(instance.onAuthWithExtraPassword(USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Failure)
    }

    @Test
    fun ignoresResultsForOtherUsers() = runTest {
        val pending = async { instance.onAuthWithExtraPassword(USER_ID) }
        runCurrent()

        instance.setState(OTHER_USER_ID, AuthWithExtraPasswordResult.Success)
        runCurrent()
        assertThat(pending.isActive).isTrue()

        instance.setState(USER_ID, AuthWithExtraPasswordResult.Failure)
        assertThat(pending.await()).isEqualTo(AuthWithExtraPasswordResult.Failure)
    }

    @Test
    fun clearUserIdDiscardsPreviousResult() = runTest {
        instance.setState(USER_ID, AuthWithExtraPasswordResult.Success)
        instance.clearUserId(USER_ID)

        val pending = async { instance.onAuthWithExtraPassword(USER_ID) }
        runCurrent()
        assertThat(pending.isActive).isTrue()

        pending.cancel()
    }

    @Test
    fun clearUserIdKeepsOtherUsersResults() = runTest {
        instance.setState(USER_ID, AuthWithExtraPasswordResult.Success)
        instance.setState(OTHER_USER_ID, AuthWithExtraPasswordResult.Failure)
        instance.clearUserId(USER_ID)

        assertThat(instance.onAuthWithExtraPassword(OTHER_USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Failure)
    }

    @Test
    fun cancelWhileWaitingReturnsFailure() = runTest {
        val pending = async { instance.onAuthWithExtraPassword(USER_ID) }
        runCurrent()

        instance.onAuthWithExtraPasswordCancelled(USER_ID)

        assertThat(pending.await()).isEqualTo(AuthWithExtraPasswordResult.Failure)
    }

    @Test
    fun cancelDoesNotOverrideExistingResult() = runTest {
        instance.setState(USER_ID, AuthWithExtraPasswordResult.Success)

        instance.onAuthWithExtraPasswordCancelled(USER_ID)

        assertThat(instance.onAuthWithExtraPassword(USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Success)
    }

    @Test
    fun cancelOnlyAffectsThatUser() = runTest {
        val pending = async { instance.onAuthWithExtraPassword(USER_ID) }
        runCurrent()

        instance.onAuthWithExtraPasswordCancelled(OTHER_USER_ID)
        runCurrent()

        assertThat(pending.isActive).isTrue()
        pending.cancel()
    }

    private companion object {
        val USER_ID = UserId("user-id")
        val OTHER_USER_ID = UserId("other-user-id")
    }
}
