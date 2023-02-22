package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.Option
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.entity.PackageInfo

data class UpdateAutofillItemData(
    val shareId: ShareId,
    val itemId: ItemId,
    val packageInfo: Option<PackageInfo>,
    val url: Option<String>,
    val shouldAssociate: Boolean
)

interface UpdateAutofillItem {
    operator fun invoke(data: UpdateAutofillItemData)
}
