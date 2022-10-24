package me.proton.pass.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAliasOptionsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Options")
    val options: AliasOptionsResponse
)

@Serializable
data class AliasOptionsResponse(
    @SerialName("Suffixes")
    val suffixes: List<AliasSuffixResponse>,
    @SerialName("Mailboxes")
    val mailboxes: List<AliasMailboxResponse>
)

@Serializable
data class AliasSuffixResponse(
    @SerialName("Suffix")
    val suffix: String,
    @SerialName("SignedSuffix")
    val signedSuffix: String,
    @SerialName("IsCustom")
    val isCustom: Boolean,
    @SerialName("Domain")
    val domain: String
)

@Serializable
data class AliasMailboxResponse(
    @SerialName("ID")
    val id: Int,
    @SerialName("Email")
    val email: String
)
