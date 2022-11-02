package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.create.alias.AliasItem

class AliasItemPreviewProvider : PreviewParameterProvider<AliasItem> {
    override val values: Sequence<AliasItem>
        get() = sequenceOf(
            AliasItem(aliasToBeCreated = ""),
            AliasItem(aliasToBeCreated = "somealias@random.local")
        )
}
