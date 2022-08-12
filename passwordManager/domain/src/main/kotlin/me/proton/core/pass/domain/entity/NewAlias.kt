package me.proton.core.pass.domain.entity

import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasSuffix

data class NewAlias(
    val title: String,
    val note: String,
    val prefix: String,
    val suffix: AliasSuffix,
    val mailbox: AliasMailbox
)
