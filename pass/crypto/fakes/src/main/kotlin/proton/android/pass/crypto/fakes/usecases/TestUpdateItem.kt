package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.pass.domain.key.ItemKey
import proton_pass_item_v1.ItemV1

class TestUpdateItem : UpdateItem {

    private var request: EncryptedUpdateItemRequest? = null

    fun setRequest(value: EncryptedUpdateItemRequest) {
        request = value
    }

    override fun createRequest(
        itemKey: ItemKey,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest = request ?: throw IllegalStateException("request is not set")
}
