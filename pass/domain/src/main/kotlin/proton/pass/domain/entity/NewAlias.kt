package proton.pass.domain.entity

import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasSuffix

data class NewAlias(
    val title: String,
    val note: String,
    val prefix: String,
    val suffix: AliasSuffix,
    val mailboxes: List<AliasMailbox>
)
