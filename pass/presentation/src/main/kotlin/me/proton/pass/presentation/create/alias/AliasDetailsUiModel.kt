package me.proton.pass.presentation.create.alias

import me.proton.pass.domain.AliasDetails

data class AliasDetailsUiModel(
    val email: String,
    val mailboxes: List<AliasMailboxUiModel>,
    val availableMailboxes: List<AliasMailboxUiModel>
) {
    constructor(aliasDetails: AliasDetails) : this(
        email = aliasDetails.email,
        mailboxes = aliasDetails.mailboxes.map(::AliasMailboxUiModel),
        availableMailboxes = aliasDetails.availableMailboxes.map(::AliasMailboxUiModel)
    )
}
