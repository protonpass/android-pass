package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class SelectorPreviewProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf(
            "",
            "some.text",
            "some.very.long.text.that.should.be.ellipsized.in.the.end.as.it.will.not.fit.in.a.line",
            """
            some.text that contains spaces
            and also multiline content
            """.trimIndent()
        )
}
