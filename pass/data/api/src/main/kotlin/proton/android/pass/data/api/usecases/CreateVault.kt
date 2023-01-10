package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault

interface CreateVault {
    suspend operator fun invoke(userId: SessionUserId, vault: NewVault): Result<Share>
}

