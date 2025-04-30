package proton.android.pass.account.fakes.auth

import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

class FakeUserCheck @Inject constructor() : PostLoginAccountSetup.UserCheck {
    override suspend fun invoke(user: User): PostLoginAccountSetup.UserCheckResult =
        PostLoginAccountSetup.UserCheckResult.Success
}
