package proton.android.pass.data.fakes.usecases

import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.domain.OrganizationKey

class FakeOpenOrganizationKey(
    private var result: Result<Pair<PrivateKey, PublicKey>> = Result.failure(IllegalStateException("Result not set"))
) : OpenOrganizationKey {
    override fun invoke(user: User, organizationKey: OrganizationKey): Result<Pair<PrivateKey, PublicKey>> = result

    fun setResult(value: Result<Pair<PrivateKey, PublicKey>>) {
        result = value
    }
}
