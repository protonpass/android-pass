package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias

interface CreateItemAndAlias {
    suspend operator fun invoke(
        userId: UserId? = null,
        shareId: ShareId,
        itemContents: ItemContents,
        newAlias: NewAlias
    ): Item
}
