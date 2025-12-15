package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.OrganizationKey
import proton.android.pass.domain.repositories.OrganizationKeyRepository

class FakeOrganizationKeyRepository : OrganizationKeyRepository {

    var organizationKey: OrganizationKey? = OrganizationKey(
        privateKey = "org-priv",
        token = "org-token",
        signature = "org-sig",
        passwordless = false
    )

    override suspend fun getOrganizationKey(userId: UserId, forceRefresh: Boolean): OrganizationKey? = organizationKey
}
