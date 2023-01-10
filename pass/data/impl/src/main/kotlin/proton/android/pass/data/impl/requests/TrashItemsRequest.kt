package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrashItemsRequest(
    @SerialName("Items")
    val items: List<TrashItemRevision>
)

@Serializable
data class TrashItemRevision(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long
)
