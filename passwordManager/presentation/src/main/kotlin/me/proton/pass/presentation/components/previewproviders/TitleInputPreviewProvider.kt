package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class TitleInputPreviewProvider :
    PreviewParameterProvider<TitleInputPreviewData> {
    override val values: Sequence<TitleInputPreviewData>
        get() = sequenceOf(
            TitleInputPreviewData(title = "", onTitleRequiredError = false),
            TitleInputPreviewData(title = "with content", onTitleRequiredError = false),
            TitleInputPreviewData(title = "", onTitleRequiredError = true)
        )
}

data class TitleInputPreviewData(
    val title: String,
    val onTitleRequiredError: Boolean
)
