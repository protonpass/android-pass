package proton.pass.domain

data class Vault(
    val shareId: ShareId,
    val name: String,
    val activeItemCount: Long,
    val trashedItemCount: Long
)
