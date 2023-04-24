package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class ThemeItemTitleProvider : ThemePairPreviewProvider<ItemTitleInput>(ItemTitlePreviewProvider())

class ItemTitlePreviewProvider : PreviewParameterProvider<ItemTitleInput> {
    override val values: Sequence<ItemTitleInput>
        get() = sequence {
            val title = "A really long title to check if the element is multiline"
            yield(ItemTitleInput(title = title, vault = null))
            yield(
                ItemTitleInput(
                    title = "A really long title to check if the element is multiline",
                    vault = Vault(
                        shareId = ShareId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        isPrimary = false
                    )
                )
            )
        }
}

data class ItemTitleInput(
    val title: String,
    val vault: Vault?
)
