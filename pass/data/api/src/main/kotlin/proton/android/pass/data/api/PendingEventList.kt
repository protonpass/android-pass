package proton.android.pass.data.api

data class PendingEventItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val keyRotation: Long,
    val content: String,
    val key: String?,
    val state: Int,
    val aliasEmail: String?,
    val createTime: Long,
    val modifyTime: Long,
    val lastUseTime: Long,
    val revisionTime: Long
)

data class PendingEventList(
    val updatedItems: List<PendingEventItemRevision>,
    val deletedItemIds: List<String>
)
