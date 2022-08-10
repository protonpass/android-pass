package me.proton.core.pass.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrashItemsResponse(
    @SerialName("Items")
    val items: List<TrashItemsRevisions>
)

@Serializable
data class TrashItemsRevisions(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long
)
