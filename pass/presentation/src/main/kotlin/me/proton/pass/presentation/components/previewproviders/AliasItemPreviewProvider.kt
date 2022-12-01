package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.create.alias.AliasItem

class AliasItemPreviewProvider : PreviewParameterProvider<AliasItemParameter> {
    override val values: Sequence<AliasItemParameter>
        get() = sequenceOf(
            with(title = "Empty alias", alias = ""),
            with(title = "With content", alias = "somealias@random.local"),
            with(
                title = "With very long content",
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local"
            )
        )

    companion object {
        private fun with(title: String, alias: String) =
            AliasItemParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("456"),
                    name = title,
                    itemType = ItemType.Alias(aliasEmail = alias), note = ""
                ),
                item = AliasItem(aliasToBeCreated = alias),
                itemType = ItemType.Alias(aliasEmail = alias)
            )
    }
}

data class AliasItemParameter(
    val model: ItemUiModel,
    val item: AliasItem,
    val itemType: ItemType.Alias
)
