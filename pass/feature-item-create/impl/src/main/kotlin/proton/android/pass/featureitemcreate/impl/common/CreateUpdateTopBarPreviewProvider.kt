package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.Purple100,
                weakestColor = PassPalette.Purple8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.Green100,
                weakestColor = PassPalette.Green8
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                opaqueColor = PassPalette.Yellow100,
                weakestColor = PassPalette.Yellow8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.Purple100,
                weakestColor = PassPalette.Purple8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.Green100,
                weakestColor = PassPalette.Green8,
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                opaqueColor = PassPalette.Yellow100,
                weakestColor = PassPalette.Yellow8
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val opaqueColor: Color,
    val weakestColor: Color
)
