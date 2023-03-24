package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.Lavender100,
                weakestColor = PassPalette.Lavender8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.GreenSheen100,
                weakestColor = PassPalette.GreenSheen8
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.MacaroniAndCheese100,
                weakestColor = PassPalette.MacaroniAndCheese8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.Lavender100,
                weakestColor = PassPalette.Lavender8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.GreenSheen100,
                weakestColor = PassPalette.GreenSheen8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.MacaroniAndCheese100,
                weakestColor = PassPalette.MacaroniAndCheese8
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val opaqueColor: Color,
    val weakestColor: Color
)
