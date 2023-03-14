package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class AliasItemPreviewProvider : PreviewParameterProvider<AliasItemParameter> {
    override val values: Sequence<AliasItemParameter>
        get() = sequenceOf(
            with(alias = ""),
            with(alias = "somealias@random.local"),
            with(
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local"
            )
        )

    companion object {
        private fun with(alias: String) =
            AliasItemParameter(
                item = AliasItem(aliasToBeCreated = alias)
            )
    }
}

data class AliasItemParameter(
    val item: AliasItem
)
