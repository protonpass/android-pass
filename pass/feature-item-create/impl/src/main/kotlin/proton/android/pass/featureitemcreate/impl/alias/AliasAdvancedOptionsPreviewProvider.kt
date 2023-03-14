package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class AliasAdvancedOptionsPreviewProvider : PreviewParameterProvider<AliasAdvancedOptionsInput> {
    override val values: Sequence<AliasAdvancedOptionsInput>
        get() = sequence {
            for (isError in listOf(true, false)) {
                for (canSelectSuffix in listOf(true, false)) {
                    for (prefix in listOf("", "prefix")) {
                        yield(
                            AliasAdvancedOptionsInput(
                                isError = isError,
                                canSelectSuffix = canSelectSuffix,
                                prefix = prefix,
                                suffix = AliasSuffixUiModel(
                                    suffix = ".some@suffix.test",
                                    signedSuffix = "",
                                    isCustom = false,
                                    domain = "suffix.test"
                                )
                            )
                        )
                    }
                }
            }
        }
}

data class AliasAdvancedOptionsInput(
    val isError: Boolean,
    val canSelectSuffix: Boolean,
    val prefix: String,
    val suffix: AliasSuffixUiModel
)
