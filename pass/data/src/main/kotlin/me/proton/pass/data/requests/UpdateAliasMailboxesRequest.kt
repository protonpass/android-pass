package me.proton.pass.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAliasMailboxesRequest(
    @SerialName("MailboxIDs")
    val mailboxIds: List<Int>
)
