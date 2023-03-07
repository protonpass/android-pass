package proton.android.pass.featurehome.impl.icon

import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareProperties

data class ActiveVaultState(
    val name: String,
    val properties: ShareProperties
) {
    companion object {
        val Initial = ActiveVaultState(
            name = "",
            properties = ShareProperties(
                shareColor = ShareColor.Purple,
                shareIcon = ShareIcon.House
            )
        )
    }
}
