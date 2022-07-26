package me.proton.core.pass.domain

sealed class ShareSelection {
    object AllShares : ShareSelection()
    data class Share(val shareId: ShareId) : ShareSelection()
}
