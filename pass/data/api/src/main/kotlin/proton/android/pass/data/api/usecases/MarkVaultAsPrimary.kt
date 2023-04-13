package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.ShareId

interface MarkVaultAsPrimary {
    suspend operator fun invoke(userId: UserId? = null, shareId: ShareId)
}
