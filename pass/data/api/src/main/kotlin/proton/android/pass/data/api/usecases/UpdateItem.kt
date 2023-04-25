package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId

interface UpdateItem {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        item: Item,
        contents: ItemContents
    ): Item
}
