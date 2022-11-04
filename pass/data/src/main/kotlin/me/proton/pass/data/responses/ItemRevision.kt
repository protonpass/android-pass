package me.proton.pass.data.responses

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
    val revisions: List<ItemRevision>
)

@Serializable
data class ItemRevision(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("Content")
    val content: String,
    @SerialName("UserSignature")
    val userSignature: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String,
    @SerialName("State")
    val state: Int,
    @SerialName("SignatureEmail")
    val signatureEmail: String,
    @SerialName("AliasEmail")
    val aliasEmail: String?,
    @SerialName("Labels")
    val labels: List<String>,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ModifyTime")
    val modifyTime: Long
)
