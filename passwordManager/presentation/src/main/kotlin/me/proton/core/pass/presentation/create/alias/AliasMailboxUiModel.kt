package me.proton.core.pass.presentation.create.alias

import me.proton.core.pass.domain.AliasMailbox

data class AliasMailboxUiModel(
    val model: AliasMailbox,
    val selected: Boolean
)
