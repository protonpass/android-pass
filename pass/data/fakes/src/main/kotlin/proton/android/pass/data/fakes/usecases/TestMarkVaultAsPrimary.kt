package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.MarkVaultAsPrimary
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestMarkVaultAsPrimary @Inject constructor() : MarkVaultAsPrimary {

    override suspend fun invoke(userId: UserId?, shareId: ShareId) {}
}
