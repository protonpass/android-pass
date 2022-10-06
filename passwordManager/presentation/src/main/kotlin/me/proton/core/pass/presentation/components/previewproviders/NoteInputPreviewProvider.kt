package me.proton.core.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NoteInputPreviewProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf(
            "",
            "Some content",
            """
                Some multiline
                Content
                That should
                Respect

                Newlines
            """.trimIndent()
        )
}
