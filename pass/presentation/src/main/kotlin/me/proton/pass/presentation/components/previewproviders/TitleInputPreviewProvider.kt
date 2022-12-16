package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class TitleInputPreviewProvider :
    PreviewParameterProvider<TitleInputPreviewData> {
    override val values: Sequence<TitleInputPreviewData>
        get() = sequence {
            for (isEditAllowed in listOf(true, false)) {
                for (data in previewData) {
                    yield(
                        TitleInputPreviewData(
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

data class TitleInputPreviewData(
    val title: String,
    val onTitleRequiredError: Boolean,
    val enabled: Boolean
)
