package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemAliasRequest(
    @SerialName("Alias")
    val alias: CreateAliasRequest,
    @SerialName("Item")
    val item: CreateItemRequest
)
