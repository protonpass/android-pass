package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias

interface CreateAlias {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        newAlias: NewAlias
    ): Item
}
