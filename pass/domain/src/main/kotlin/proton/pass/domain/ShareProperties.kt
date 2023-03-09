package proton.pass.domain

enum class ShareColor {
    Color1,
    Color2,
    Color3,
    Color4,
    Color5,
    Color6,
    Color7,
    Color8,
    Color9,
    Color10
}

enum class ShareIcon {
    Icon1,
    Icon2,
    Icon3,
    Icon4,
    Icon5,
    Icon6,
    Icon7,
    Icon8,
    Icon9,
    Icon10
}

data class ShareProperties(
    val shareColor: ShareColor,
    val shareIcon: ShareIcon
)
