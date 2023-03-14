package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class SelectorPreviewProvider : PreviewParameterProvider<SelectorPreviewParameter> {
    override val values: Sequence<SelectorPreviewParameter>
        get() = sequence {
            for (enabled in listOf(true, false)) {
                for (text in texts) {
                    yield(
                        SelectorPreviewParameter(
                            text = text,
                            enabled = enabled
                        )
                    )
                }
            }
        }


    private val texts = listOf(
        "",
        "some.text",
        "some.very.long.text.that.should.be.ellipsized.in.the.end.as.it.will.not.fit.in.a.line",
        """
        some.text that contains spaces
        and also multiline content
        """.trimIndent()
    )
}

data class SelectorPreviewParameter(
    val text: String,
    val enabled: Boolean
)
