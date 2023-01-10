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
