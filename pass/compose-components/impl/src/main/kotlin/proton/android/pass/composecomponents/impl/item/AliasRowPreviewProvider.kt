package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

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
            ),
            with(
                title = "With multiline content to check highlight",
                alias = "somealias.withsuffix.thatisverylong.",
                note = "A note \n with \n multiline \n text \n to \n verify " +
                    "\n that the \n word \n local \n is highlighted",
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
                    itemType = ItemType.Alias(aliasEmail = alias),
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                highlight = highlight
            )
    }
}

data class AliasRowParameter(
    val model: ItemUiModel,
    val highlight: String
)
