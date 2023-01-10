package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestUpdateAutofillItem @Inject constructor() : UpdateAutofillItem {

    override fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData) {}
}
