package me.proton.android.pass.data.api

data class ItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val rotationId: String,
    val content: String,
    val userSignature: String,
    val itemKeySignature: String,
    val state: Int,
    val signatureEmail: String,
    val aliasEmail: String?,
    val labels: List<String>,
    val createTime: Long,
    val modifyTime: Long
)

data class PendingEventList(
    val updatedItems: List<ItemRevision>,
    val deletedItemIds: List<String>
)
