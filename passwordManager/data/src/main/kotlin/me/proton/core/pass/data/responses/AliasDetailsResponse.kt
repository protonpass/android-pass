package me.proton.core.pass.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AliasDetailsResponse(
    @SerialName("Alias")
    val alias: AliasDetails
)

@Serializable
data class AliasDetails(
    @SerialName("Email")
    val email: String,
    @SerialName("Mailboxes")
    val mailboxes: List<AliasMailboxResponse>
)
