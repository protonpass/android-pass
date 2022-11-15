package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NoteInputPreviewProvider : PreviewParameterProvider<NoteInputPreviewParameter> {
    override val values: Sequence<NoteInputPreviewParameter>
        get() = sequenceOf(
            NoteInputPreviewParameter(
                value = "", enabled = true
            ),
            NoteInputPreviewParameter(
                value = "Some content", enabled = true
            ),
            NoteInputPreviewParameter(
                value = "Some content", enabled = false
            ),
            NoteInputPreviewParameter(
                value = """
                Some multiline
                Content
                That should
                Respect

                Newlines
                """.trimIndent(),
                enabled = true
            )
        )
}

data class NoteInputPreviewParameter(
    val value: String,
    val enabled: Boolean
)
