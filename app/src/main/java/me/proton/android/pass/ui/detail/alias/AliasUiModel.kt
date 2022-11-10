package me.proton.android.pass.ui.detail.alias

import me.proton.pass.domain.AliasMailbox

data class AliasUiModel(
    val title: String,
    val alias: String,
    val mailboxes: List<AliasMailbox>,
    val note: String
)
