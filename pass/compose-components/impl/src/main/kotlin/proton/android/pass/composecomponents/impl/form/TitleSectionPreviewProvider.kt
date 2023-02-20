package proton.android.pass.composecomponents.impl.form

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class TitleSectionPreviewProvider :
    PreviewParameterProvider<TitleSectionPreviewData> {
    override val values: Sequence<TitleSectionPreviewData>
        get() = sequence {
            for (isEditAllowed in listOf(true, false)) {
                for (data in previewData) {
                    yield(
                        TitleSectionPreviewData(
                            title = data.first,
                            onTitleRequiredError = data.second,
                            enabled = isEditAllowed
                        )
                    )
                }
            }
        }

    private val previewData = listOf(
        "" to false,
        "with content" to false,
        "" to true
    )
}

data class TitleSectionPreviewData(
    val title: String,
    val onTitleRequiredError: Boolean,
    val enabled: Boolean
)
