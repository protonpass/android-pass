package me.proton.pass.presentation.create.alias

import me.proton.pass.domain.AliasMailbox

data class AliasMailboxUiModel(
    val model: AliasMailbox,
    val selected: Boolean
)
