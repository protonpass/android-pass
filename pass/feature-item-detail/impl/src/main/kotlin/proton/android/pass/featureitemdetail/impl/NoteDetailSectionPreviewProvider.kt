package proton.android.pass.featureitemdetail.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NoteDetailSectionPreviewProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf(
            "",
            "Some note",
            "Some very long note that contains a lot of text in a single line and it should " +
                "be converted into multiline",
            """
                Some note
                That contains text
                In multiple lines
                
                And even contains a very long line that should also appear in a new line expecting
                nothing will break and the UI will be able to handle it
            """.trimIndent()
        )
}
