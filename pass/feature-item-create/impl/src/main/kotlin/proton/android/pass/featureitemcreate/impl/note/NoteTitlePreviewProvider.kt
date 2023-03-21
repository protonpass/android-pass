package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NoteTitlePreviewProvider : PreviewParameterProvider<NoteTitleInput> {
    override val values: Sequence<NoteTitleInput>
        get() = sequence {
            for (enabled in listOf(true, false)) {
                for (isError in listOf(true, false)) {
                    for (text in TEXTS) {
                        yield(NoteTitleInput(enabled = enabled, isError = isError, text = text))
                    }
                }
            }
        }

    companion object {
        private val TEXTS = listOf(
            "",
            "some text",
            "some text that should wrap into more than one line so we can see if it supports multiline"
        )
    }
}

data class NoteTitleInput(
    val enabled: Boolean,
    val isError: Boolean,
    val text: String
)
