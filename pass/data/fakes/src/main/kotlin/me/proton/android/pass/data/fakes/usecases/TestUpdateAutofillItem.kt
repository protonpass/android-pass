package me.proton.android.pass.data.fakes.usecases

import me.proton.android.pass.data.api.usecases.UpdateAutofillItem
import me.proton.android.pass.data.api.usecases.UpdateAutofillItemData
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class TestUpdateAutofillItem @Inject constructor() : UpdateAutofillItem {

    override fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData) {}
}
