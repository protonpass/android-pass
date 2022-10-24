package me.proton.pass.data.responses

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
    val revision: Long,
    @SerialName("State")
    val state: Int,
    @SerialName("ModifyTime")
    val modifyTime: Int,
    @SerialName("RevisionTime")
    val revisionTime: Int
)
