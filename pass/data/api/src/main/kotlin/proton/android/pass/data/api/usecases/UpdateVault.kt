package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

interface UpdateVault {
    suspend operator fun invoke(
        userId: SessionUserId? = null,
        shareId: ShareId,
        vault: NewVault
    ): Share
}
