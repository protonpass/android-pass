package proton.android.pass.commonuimodels.api

import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

data class ShareUiModel(
    val id: ShareId,
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val isPrimary: Boolean
) {
    companion object {
        fun fromVault(vault: Vault) = ShareUiModel(
            id = vault.shareId,
            name = vault.name,
            color = vault.color,
            icon = vault.icon,
            isPrimary = vault.isPrimary
        )
    }
}

data class ShareUiModelWithItemCount(
    val id: ShareId,
    val name: String,
    val activeItemCount: Long,
    val trashedItemCount: Long,
    val color: ShareColor,
    val icon: ShareIcon,
    val isPrimary: Boolean
)
