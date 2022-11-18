package me.proton.android.pass.data.api.usecases

import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName

data class UpdateAutofillItemData(
    val packageName: Option<PackageName>,
    val url: Option<String>
)

interface UpdateAutofillItem {
    operator fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData)
}
