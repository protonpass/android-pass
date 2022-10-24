package me.proton.pass.domain.entity

import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasSuffix

data class NewAlias(
    val title: String,
    val note: String,
    val prefix: String,
    val suffix: AliasSuffix,
    val mailboxes: List<AliasMailbox>
)
