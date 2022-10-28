package me.proton.pass.domain

data class AliasDetails(
    val email: String,
    val mailboxes: List<AliasMailbox>,
    val availableMailboxes: List<AliasMailbox>
)
