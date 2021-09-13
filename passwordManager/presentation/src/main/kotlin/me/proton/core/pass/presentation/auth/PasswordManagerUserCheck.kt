package me.proton.core.pass.presentation.auth

import android.content.Context
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.presentation.DefaultUserCheck
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.auth.PasswordManagerUserCheck.Companion.SCOPE_PASS
import me.proton.core.pass.presentation.auth.PasswordManagerUserCheck.Companion.SCOPE_FULL
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User

/**
 * PasswordManager API requires following scopes: "full", "nondelinquent" and "drive".
 * During the sign in process we check for "full" and "pass" scope. Delinquent user is
 * checked within [DefaultUserCheck].
 */
class PasswordManagerUserCheck(
    private val context: Context,
    private val sessionManager: SessionManager,
    accountManager: AccountManager,
    userManager: UserManager
) : DefaultUserCheck(context, accountManager, userManager) {

    override suspend fun invoke(user: User): SetupAccountCheck.UserCheckResult = when {
        !sessionManager.hasPassScope(user.userId) -> errorNoPassScope()
        else -> super.invoke(user)
    }

    private fun errorNoPassScope() = SetupAccountCheck.UserCheckResult.Error(
        TODO("Use actual error message")
    )

    companion object {
        // TODO: Replace "drive" with "pass" once the backend is ready
        const val SCOPE_PASS = "drive"
        const val SCOPE_FULL = "full"
    }
}

suspend fun SessionManager.hasPassScope(userId: UserId): Boolean =
    getSessionId(userId)?.run {
        getSession(this)?.run {
            scopes.containsAll(listOf(SCOPE_FULL, SCOPE_PASS))
        }
    } ?: false