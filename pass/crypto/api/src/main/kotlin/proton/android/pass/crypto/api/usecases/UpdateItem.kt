package proton.android.pass.crypto.api.usecases

import proton.pass.domain.key.ItemKey
import proton_pass_item_v1.ItemV1

data class EncryptedUpdateItemRequest(
    val keyRotation: Long,
    val lastRevision: Long,
    val contentFormatVersion: Int,
    val content: String,
)

interface UpdateItem {
    fun createRequest(
        itemKey: ItemKey,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest
}
