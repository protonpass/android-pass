package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result

interface GetCurrentUserId {
    suspend operator fun invoke(): Result<UserId>
}

