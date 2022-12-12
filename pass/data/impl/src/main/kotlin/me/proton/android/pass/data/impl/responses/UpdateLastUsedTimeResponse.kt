package me.proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateLastUsedTimeResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Revision")
    val revision: ItemRevision
)
