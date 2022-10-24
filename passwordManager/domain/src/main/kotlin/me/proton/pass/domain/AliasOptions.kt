package me.proton.pass.domain

data class AliasOptions(
    val suffixes: List<AliasSuffix>,
    val mailboxes: List<AliasMailbox>
)

data class AliasSuffix(
    val suffix: String,
    val signedSuffix: String,
    val isCustom: Boolean,
    val domain: String
)

data class AliasMailbox(
    val id: Int,
    val email: String
)
