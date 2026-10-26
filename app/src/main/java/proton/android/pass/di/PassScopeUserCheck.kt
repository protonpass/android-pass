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

package proton.android.pass.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultUserCheck
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import proton.android.pass.R
import javax.inject.Inject

class PassScopeUserCheck @Inject constructor(
    accountManager: AccountManager,
    private val passScopeCheck: PassScopeCheck,
    @ApplicationContext private val context: Context,
    userManager: UserManager
) : DefaultUserCheck(context, accountManager, userManager) {
    override suspend fun invoke(user: User): PostLoginAccountSetup.UserCheckResult =
        when (val superResult = super.invoke(user)) {
            is PostLoginAccountSetup.UserCheckResult.Success -> passScopeCheck(user).toUserCheckResult()
            else -> superResult
        }

    private fun PassScopeCheckResult.toUserCheckResult(): PostLoginAccountSetup.UserCheckResult = when (this) {
        PassScopeCheckResult.Passed -> PostLoginAccountSetup.UserCheckResult.Success
        PassScopeCheckResult.NoAccountFound -> error(R.string.no_account_found)
        PassScopeCheckResult.NoSessionFound -> error(R.string.no_session_found)
        PassScopeCheckResult.ExtraPasswordFailed -> error(R.string.auth_with_extra_password_failed)
        PassScopeCheckResult.FreeUserLimitReached -> error(R.string.auth_only_1_proton_account_failed)
    }

    private fun error(messageRes: Int): PostLoginAccountSetup.UserCheckResult =
        PostLoginAccountSetup.UserCheckResult.Error(context.getString(messageRes))
}
