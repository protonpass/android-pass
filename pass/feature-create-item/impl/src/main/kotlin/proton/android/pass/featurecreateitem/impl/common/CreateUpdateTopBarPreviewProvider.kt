package proton.android.pass.featurecreateitem.impl.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassPalette.Purple100
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassPalette.Green100
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassPalette.Yellow100
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassPalette.Purple100
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassPalette.Green100
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassPalette.Yellow100
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val color: Color
)
