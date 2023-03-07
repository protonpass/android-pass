package proton.pass.domain

enum class ShareColor {
    Purple,
    Yellow,
    Blue,
    Green
}

enum class ShareIcon {
    House,
    Suitcase,
}

data class ShareProperties(
    val shareColor: ShareColor,
    val shareIcon: ShareIcon
)
