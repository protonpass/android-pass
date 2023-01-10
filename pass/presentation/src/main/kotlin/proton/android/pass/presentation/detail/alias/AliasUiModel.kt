package proton.android.pass.presentation.detail.alias

import proton.pass.domain.AliasMailbox

data class AliasUiModel(
    val title: String,
    val alias: String,
    val mailboxes: List<AliasMailbox>,
    val note: String
)
