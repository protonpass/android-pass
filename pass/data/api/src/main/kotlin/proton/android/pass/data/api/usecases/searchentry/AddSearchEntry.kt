package proton.android.pass.data.api.usecases.searchentry

import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface AddSearchEntry {
    suspend operator fun invoke(shareId: ShareId, itemId: ItemId)
}
