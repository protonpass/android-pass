package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Item")
    val item: ItemRevision
)

@Serializable
data class GetItemsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Items")
    val items: ItemsList
)

@Serializable
data class ItemsList(
    @SerialName("Total")
    val total: Long,
    @SerialName("RevisionsData")
    val revisions: List<ItemRevision>,
    @SerialName("LastToken")
    val lastToken: String?
)

@Serializable
data class ItemRevision(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("Content")
    val content: String,
    @SerialName("ItemKey")
    val itemKey: String?,
    @SerialName("State")
    val state: Int,
    @SerialName("AliasEmail")
    val aliasEmail: String?,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ModifyTime")
    val modifyTime: Long,
    @SerialName("LastUseTime")
    val lastUseTime: Long?,
    @SerialName("RevisionTime")
    val revisionTime: Long
)
