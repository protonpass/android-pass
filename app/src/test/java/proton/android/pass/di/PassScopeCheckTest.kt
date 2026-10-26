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

package proton.android.pass.di

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.accountmanager.FakeSessionManager
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.data.fakes.usecases.FakeAuthWithExtraPasswordListener
import proton.android.pass.data.fakes.usecases.FakeCheckFreeUserLimit
import proton.android.pass.enterextrapassword.ExtraPasswordPrompt
import proton.android.pass.test.domain.UserTestFactory

class PassScopeCheckTest {

    private lateinit var accountManager: FakeAccountManager
    private lateinit var sessionManager: FakeSessionManager
    private lateinit var listener: FakeAuthWithExtraPasswordListener
    private lateinit var checkFreeUserLimit: FakeCheckFreeUserLimit
    private lateinit var instance: PassScopeCheck

    private val user = UserTestFactory.createCore(userId = USER_ID)

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        sessionManager = FakeSessionManager()
        listener = FakeAuthWithExtraPasswordListener()
        checkFreeUserLimit = FakeCheckFreeUserLimit()
        instance = PassScopeCheck(
            accountManager = accountManager,
            sessionManager = sessionManager,
            authWithExtraPasswordListener = listener,
            extraPasswordPrompt = ExtraPasswordPrompt { listener.calls.add("start") },
            checkFreeUserLimit = checkFreeUserLimit
        )
    }

    @Test
    fun `returns NoAccountFound when the user has no account`() = runTest {
        accountManager.setAccount(USER_ID, null)

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.NoAccountFound)
    }

    @Test
    fun `returns NoSessionFound when the account has no session`() = runTest {
        accountManager.setAccount(USER_ID, FakeAccountManager.createAccount(USER_ID))

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.NoSessionFound)
    }

    @Test
    fun `returns Passed when the session has the pass scope and the limit is not reached`() = runTest {
        givenSession(scopes = listOf(PASS_SCOPE))

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.Passed)
        assertThat(listener.calls).isEmpty()
    }

    @Test
    fun `returns FreeUserLimitReached when the session has the pass scope and the limit is reached`() = runTest {
        givenSession(scopes = listOf(PASS_SCOPE))
        checkFreeUserLimit.setLimitReached(true)

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.FreeUserLimitReached)
    }

    @Test
    fun `prompts for the extra password when the pass scope is missing`() = runTest {
        givenSession(scopes = listOf("other"))

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.Passed)
        assertThat(listener.calls).containsExactly("clear", "start", "await", "clear").inOrder()
    }

    @Test
    fun `returns ExtraPasswordFailed when the extra password fails`() = runTest {
        givenSession(scopes = emptyList())
        listener.result = AuthWithExtraPasswordResult.Failure

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.ExtraPasswordFailed)
        assertThat(listener.calls).containsExactly("clear", "start", "await", "clear").inOrder()
    }

    @Test
    fun `checks the free user limit after a successful extra password`() = runTest {
        givenSession(scopes = emptyList())
        checkFreeUserLimit.setLimitReached(true)

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.FreeUserLimitReached)
    }

    @Test
    fun `does not check the free user limit when the extra password fails`() = runTest {
        givenSession(scopes = emptyList())
        listener.result = AuthWithExtraPasswordResult.Failure
        checkFreeUserLimit.setLimitReached(true)

        assertThat(instance(user)).isEqualTo(PassScopeCheckResult.ExtraPasswordFailed)
    }

    private fun givenSession(scopes: List<String>) {
        val sessionId = SessionId(SESSION_ID)
        accountManager.setAccount(
            USER_ID,
            FakeAccountManager.createAccount(USER_ID).copy(sessionId = sessionId)
        )
        sessionManager.setSession(
            Session.Authenticated(
                userId = USER_ID,
                sessionId = sessionId,
                accessToken = "access",
                refreshToken = "refresh",
                scopes = scopes
            )
        )
    }

    private companion object {
        val USER_ID = UserId("scope-check-user")
        const val SESSION_ID = "scope-check-session"
        const val PASS_SCOPE = "pass"
    }
}
