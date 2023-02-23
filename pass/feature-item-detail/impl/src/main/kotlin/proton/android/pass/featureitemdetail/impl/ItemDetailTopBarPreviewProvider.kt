package proton.android.pass.featureitemdetail.impl

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class ItemDetailTopBarPreviewProvider : PreviewParameterProvider<ItemDetailTopBarPreview> {
    override val values: Sequence<ItemDetailTopBarPreview>
        get() = sequenceOf(
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassPalette.Purple100
            ),
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassPalette.Green100
            ),
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassPalette.Yellow100
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassPalette.Purple100
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassPalette.Green100
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassPalette.Yellow100
            )
        )
}

data class ItemDetailTopBarPreview(
    val isLoading: Boolean,
    val color: Color
)
