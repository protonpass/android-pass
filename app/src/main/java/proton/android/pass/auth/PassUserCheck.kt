/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.auth

import android.content.Context
import proton.android.pass.R
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultUserCheck
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User

class PassUserCheck(
    private val context: Context,
    private val sessionManager: SessionManager,
    accountManager: AccountManager,
    userManager: UserManager
) : DefaultUserCheck(context, accountManager, userManager) {

    override suspend fun invoke(user: User): PostLoginAccountSetup.UserCheckResult =
        if (!sessionManager.hasPassScope(user.userId)) {
            errorNoPassScope()
        } else {
            super.invoke(user)
        }

    private suspend fun SessionManager.hasPassScope(userId: UserId): Boolean =
        getSessionId(userId)?.let { sessionId ->
            getSession(sessionId)?.run {
                scopes.containsAll(listOf(SCOPE_FULL, SCOPE_PASS))
            }
        } ?: false

    private fun errorNoPassScope() = PostLoginAccountSetup.UserCheckResult.Error(
        localizedMessage = context.getString(R.string.description_no_pass_scope)
    )

    companion object {
        private const val SCOPE_PASS = "pass"
        private const val SCOPE_FULL = "full"
    }

}
