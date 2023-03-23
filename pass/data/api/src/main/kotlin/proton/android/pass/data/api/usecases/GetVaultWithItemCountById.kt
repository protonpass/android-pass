package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

interface GetVaultWithItemCountById {
    operator fun invoke(userId: UserId? = null, shareId: ShareId): Flow<VaultWithItemCount>
}
