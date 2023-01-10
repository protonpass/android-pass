package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAliasMailboxesRequest(
    @SerialName("MailboxIDs")
    val mailboxIds: List<Int>
)
