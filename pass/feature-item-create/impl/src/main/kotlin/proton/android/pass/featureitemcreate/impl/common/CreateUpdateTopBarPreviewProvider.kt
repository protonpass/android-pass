package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.Lavender100,
                closeIconColor = PassPalette.Lavender100,
                closeBackgroundColor = PassPalette.Lavender8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.MacaroniAndCheese100,
                closeIconColor = PassPalette.MacaroniAndCheese100,
                closeBackgroundColor = PassPalette.MacaroniAndCheese8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.Lavender100,
                closeIconColor = PassPalette.Lavender100,
                closeBackgroundColor = PassPalette.Lavender8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.MacaroniAndCheese100,
                closeIconColor = PassPalette.MacaroniAndCheese100,
                closeBackgroundColor = PassPalette.MacaroniAndCheese8
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val actionColor: Color,
    val closeBackgroundColor: Color,
    val closeIconColor: Color,
)
