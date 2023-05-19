package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface CanDisplayTotp {
    operator fun invoke(userId: UserId? = null, shareId: ShareId, itemId: ItemId): Flow<Boolean>
}
