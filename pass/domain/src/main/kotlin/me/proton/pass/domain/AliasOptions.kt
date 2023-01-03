package me.proton.pass.domain

data class AliasOptions(
    val suffixes: List<AliasSuffix>,
    val mailboxes: List<AliasMailbox>
)
