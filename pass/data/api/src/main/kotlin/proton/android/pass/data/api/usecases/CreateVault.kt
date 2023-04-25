package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault

interface CreateVault {
    suspend operator fun invoke(userId: SessionUserId? = null, vault: NewVault): Share
}

