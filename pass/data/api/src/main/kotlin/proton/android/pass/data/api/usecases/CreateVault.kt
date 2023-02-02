package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault

interface CreateVault {
    suspend operator fun invoke(userId: SessionUserId? = null, vault: NewVault): LoadingResult<Share>
}

