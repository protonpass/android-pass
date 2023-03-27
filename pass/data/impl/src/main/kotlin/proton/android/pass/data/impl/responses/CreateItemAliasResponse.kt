package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemAliasResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Bundle")
    val bundle: CreateItemAliasBundle
)

@Serializable
data class CreateItemAliasBundle(
    @SerialName("Alias")
    val alias: ItemRevision,
    @SerialName("Item")
    val item: ItemRevision
)
