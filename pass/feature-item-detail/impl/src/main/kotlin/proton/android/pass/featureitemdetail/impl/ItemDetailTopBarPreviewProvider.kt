package proton.android.pass.featureitemdetail.impl

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassPalette

class ItemDetailTopBarPreviewProvider : PreviewParameterProvider<ItemDetailTopBarPreview> {
    override val values: Sequence<ItemDetailTopBarPreview>
        get() = sequence {
            for (isLoading in listOf(true, false)) {
                for (
                    color in listOf(
                        PassPalette.Lavender100,
                        PassPalette.GreenSheen100,
                        PassPalette.MacaroniAndCheese100
                    )
                ) {
                    yield(
                        ItemDetailTopBarPreview(
                            isLoading = isLoading,
                            color = color,
                            closeBackgroundColor = color.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
}

data class ItemDetailTopBarPreview(
    val isLoading: Boolean,
    val color: Color,
    val closeBackgroundColor: Color
)
