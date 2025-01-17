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

package proton.android.pass.di

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultUserCheck
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.extension.isCredentialLess
import proton.android.pass.PassActivityOrchestrator
import proton.android.pass.R
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordListener
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.log.api.PassLogger

class PassScopeUserCheck(
    private val accountManager: AccountManager,
    private val sessionManager: SessionManager,
    private val authWithExtraPasswordListener: AuthWithExtraPasswordListener,
    private val passActivityOrchestrator: PassActivityOrchestrator,
    private val context: Context,
    private val userManager: UserManager
) : DefaultUserCheck(context, accountManager, userManager) {
    override suspend fun invoke(user: User): PostLoginAccountSetup.UserCheckResult =
        when (val superResult = super.invoke(user)) {
            is PostLoginAccountSetup.UserCheckResult.Success -> {
                when (val passScopeResult = checkPassScope(user, authWithExtraPasswordListener)) {
                    is PostLoginAccountSetup.UserCheckResult.Success -> performAdditionalChecks()
                    else -> passScopeResult
                }
            }

            else -> superResult
        }

    private suspend fun checkPassScope(
        user: User,
        authWithExtraPasswordListener: AuthWithExtraPasswordListener
    ): PostLoginAccountSetup.UserCheckResult {
        val account = accountManager.getAccount(user.userId).firstOrNull()
            ?: return PostLoginAccountSetup.UserCheckResult.Error(
                context.getString(R.string.no_account_found)
            )
        val session = sessionManager.getSession(account.sessionId)
            ?: return PostLoginAccountSetup.UserCheckResult.Error(
                context.getString(R.string.no_session_found)
            )

        return if (session.scopes.contains(PASS_SCOPE)) {
            PostLoginAccountSetup.UserCheckResult.Success
        } else {
            PassLogger.i(TAG, "Waiting for extra password to be ready")

            passActivityOrchestrator.startEnterExtraPassword(user.userId)

            val res = authWithExtraPasswordListener.onAuthWithExtraPassword(user.userId)
            authWithExtraPasswordListener.clearUserId(user.userId)
            PassLogger.i(TAG, "Auth with extra password listener: $res")
            when (res) {
                AuthWithExtraPasswordResult.Failure -> {
                    PostLoginAccountSetup.UserCheckResult.Error(
                        context.getString(R.string.auth_with_extra_password_failed)
                    )
                }

                AuthWithExtraPasswordResult.Success -> {
                    PostLoginAccountSetup.UserCheckResult.Success
                }
            }
        }
    }

    private suspend fun performAdditionalChecks(): PostLoginAccountSetup.UserCheckResult = when {
        currentFreeUserCount() >= 1 -> PostLoginAccountSetup.UserCheckResult.Error(
            context.getString(R.string.auth_only_1_proton_account_failed)
        )
        else -> PostLoginAccountSetup.UserCheckResult.Success
    }

    private suspend fun currentFreeUserCount(): Int = accountManager.getAccounts(AccountState.Ready).first()
        .map { userManager.getUser(it.userId) }
        .filterNot { it.isCredentialLess() }
        .fold(0) { acc, user ->
            val hasPassLifeTime = user.flags["pass-lifetime"] ?: false
            val isPassFromSL = user.flags["pass-from-sl"] ?: false
            if (!user.hasSubscription() && !hasPassLifeTime && !isPassFromSL) {
                acc + 1
            } else {
                acc
            }
        }

    companion object {
        private const val TAG = "PassScopeUserCheck"
        private const val PASS_SCOPE = "pass"
    }
}
