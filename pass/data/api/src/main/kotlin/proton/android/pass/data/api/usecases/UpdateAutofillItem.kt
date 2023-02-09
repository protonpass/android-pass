package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.Option
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.entity.PackageName

data class UpdateAutofillItemData(
    val shareId: ShareId,
    val itemId: ItemId,
    val packageName: Option<PackageName>,
    val url: Option<String>,
    val shouldAssociate: Boolean
)

interface UpdateAutofillItem {
    operator fun invoke(data: UpdateAutofillItemData)
}
