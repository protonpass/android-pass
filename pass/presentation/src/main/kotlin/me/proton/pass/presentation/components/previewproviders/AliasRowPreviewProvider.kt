package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.create.alias.AliasItem

class AliasRowPreviewProvider : PreviewParameterProvider<AliasRowParameter> {
    override val values: Sequence<AliasRowParameter>
        get() = sequenceOf(
            with(title = "Empty alias", alias = ""),
            with(title = "With content", alias = "somealias@random.local"),
            with(
                title = "With very long content",
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local"
            ),
            with(
                title = "With very long content to check highlight",
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local",
                note = "A note with a long text to verify that the word local is highlighted",
                highlight = "local"
            )
        )

    companion object {
        private fun with(title: String, alias: String, note: String = "", highlight: String = "") =
            AliasRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("456"),
                    name = title,
                    note = note,
                    itemType = ItemType.Alias(aliasEmail = alias)
                ),
                item = AliasItem(aliasToBeCreated = alias),
                highlight = highlight
            )
    }
}

data class AliasRowParameter(
    val model: ItemUiModel,
    val item: AliasItem,
    val highlight: String
)
