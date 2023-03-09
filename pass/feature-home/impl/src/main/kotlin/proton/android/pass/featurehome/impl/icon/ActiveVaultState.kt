package proton.android.pass.featurehome.impl.icon

import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareProperties

data class ActiveVaultState(
    val properties: ShareProperties
) {
    companion object {
        val Initial = ActiveVaultState(
            properties = ShareProperties(
                shareColor = ShareColor.Color1,
                shareIcon = ShareIcon.Icon3
            )
        )
    }
}
