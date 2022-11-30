package me.proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetEventsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Events")
    val events: EventList
)

@Serializable
data class EventList(
    @SerialName("UpdatedItems")
    val updatedItems: List<ItemRevision>,
    @SerialName("DeletedItemIDs")
    val deletedItemIds: List<String>,
    @SerialName("NewRotationID")
    val newRotationId: String,
    @SerialName("LatestEventId")
    val latestEventId: String,
    @SerialName("EventsPending")
    val eventsPending: Boolean,
    @SerialName("FullRefresh")
    val fullRefresh: Boolean
)

