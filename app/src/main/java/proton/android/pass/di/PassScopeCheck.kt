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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.user.domain.entity.User
import proton.android.pass.data.api.usecases.CheckFreeUserLimit
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordListener
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.data.impl.account.toDomain
import proton.android.pass.enterextrapassword.ExtraPasswordPrompt
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

sealed interface PassScopeCheckResult {
    data object Passed : PassScopeCheckResult
    data object NoAccountFound : PassScopeCheckResult
    data object NoSessionFound : PassScopeCheckResult
    data object ExtraPasswordFailed : PassScopeCheckResult
    data object FreeUserLimitReached : PassScopeCheckResult
}

class PassScopeCheck @Inject constructor(
    private val accountManager: AccountManager,
    private val sessionManager: SessionManager,
    private val authWithExtraPasswordListener: AuthWithExtraPasswordListener,
    private val extraPasswordPrompt: ExtraPasswordPrompt,
    private val checkFreeUserLimit: CheckFreeUserLimit
) {

    suspend operator fun invoke(user: User): PassScopeCheckResult = when (val passScopeResult = checkPassScope(user)) {
        PassScopeCheckResult.Passed -> performAdditionalChecks(user)
        else -> passScopeResult
    }

    private suspend fun checkPassScope(user: User): PassScopeCheckResult {
        val account = accountManager.getAccount(user.userId).firstOrNull()
            ?: return PassScopeCheckResult.NoAccountFound
        val session = sessionManager.getSession(account.sessionId)
            ?: return PassScopeCheckResult.NoSessionFound

        return if (session.scopes.contains(PASS_SCOPE)) {
            PassScopeCheckResult.Passed
        } else {
            PassLogger.i(TAG, "Waiting for extra password to be ready")

            authWithExtraPasswordListener.clearUserId(user.userId)
            extraPasswordPrompt.start(user.userId)

            val res = authWithExtraPasswordListener.onAuthWithExtraPassword(user.userId)
            authWithExtraPasswordListener.clearUserId(user.userId)
            PassLogger.i(TAG, "Auth with extra password listener: $res")
            when (res) {
                AuthWithExtraPasswordResult.Failure -> PassScopeCheckResult.ExtraPasswordFailed
                AuthWithExtraPasswordResult.Success -> PassScopeCheckResult.Passed
            }
        }
    }

    private suspend fun performAdditionalChecks(user: User): PassScopeCheckResult = when {
        checkFreeUserLimit(user.toDomain()) -> PassScopeCheckResult.FreeUserLimitReached
        else -> PassScopeCheckResult.Passed
    }

    private companion object {
        private const val TAG = "PassScopeUserCheck"
        private const val PASS_SCOPE = "pass"
    }
}
