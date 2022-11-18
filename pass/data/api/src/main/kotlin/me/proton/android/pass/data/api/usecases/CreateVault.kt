package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.SessionUserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.entity.NewVault

interface CreateVault {
    suspend operator fun invoke(userId: SessionUserId, vault: NewVault): Result<Share>
}

