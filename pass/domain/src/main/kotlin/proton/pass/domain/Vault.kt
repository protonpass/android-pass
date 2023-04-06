package proton.pass.domain

data class Vault(
    val shareId: ShareId,
    val name: String,
    val color: ShareColor = ShareColor.Color1,
    val icon: ShareIcon = ShareIcon.Icon1,
    val isPrimary: Boolean
)

fun List<Vault>.sorted(): List<Vault> = sortedBy { it.name.lowercase() }
