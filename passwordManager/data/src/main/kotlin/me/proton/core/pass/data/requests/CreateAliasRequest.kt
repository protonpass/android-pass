package me.proton.core.pass.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAliasRequest(
    @SerialName("Prefix")
    val prefix: String,
    @SerialName("SignedSuffix")
    val signedSuffix: String,
    @SerialName("Mailboxes")
    val mailboxes: List<Int>,
    @SerialName("Item")
    val item: CreateItemRequest
)
