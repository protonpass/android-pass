package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel

class AliasToBeCreatedPreviewProvider : PreviewParameterProvider<AliasToBeCreatedInput> {
    override val values: Sequence<AliasToBeCreatedInput>
        get() = sequenceOf(
            AliasToBeCreatedInput(prefix = "", suffix = null),
            AliasToBeCreatedInput(prefix = "prefix", suffix = null),
            AliasToBeCreatedInput(prefix = "", suffix = suffix(".some@suffix.test")),
            AliasToBeCreatedInput(prefix = "prefix", suffix = suffix(".some@suffix.test")),
            AliasToBeCreatedInput(
                prefix = "prefix.that.is.super.long.in.order.to.trigger.newlines",
                suffix = suffix(".some@suffix.test")
            ),
        )

    private fun suffix(suffix: String) = AliasSuffixUiModel(
        suffix = suffix,
        signedSuffix = "",
        isCustom = false,
        domain = ""
    )
}

data class AliasToBeCreatedInput(
    val prefix: String,
    val suffix: AliasSuffixUiModel?
)
