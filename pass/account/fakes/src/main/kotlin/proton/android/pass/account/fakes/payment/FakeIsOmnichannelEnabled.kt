package proton.android.pass.account.fakes.payment

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeIsOmnichannelEnabled @Inject constructor() : IsOmnichannelEnabled {
    override fun invoke(userId: UserId?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLocalEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isRemoteEnabled(userId: UserId?): Boolean {
        TODO("Not yet implemented")
    }
}
