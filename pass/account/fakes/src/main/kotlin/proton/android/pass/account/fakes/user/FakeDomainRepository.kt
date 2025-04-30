package proton.android.pass.account.fakes.user

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeDomainRepository @Inject constructor() : DomainRepository {
    override suspend fun getAvailableDomains(sessionUserId: SessionUserId?): List<Domain> {
        TODO("Not yet implemented")
    }
}
