package proton.android.pass.featureitemdetail.impl

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassColors

class ItemDetailTopBarPreviewProvider : PreviewParameterProvider<ItemDetailTopBarPreview> {
    override val values: Sequence<ItemDetailTopBarPreview>
        get() = sequenceOf(
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassColors.PurpleAccent
            ),
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassColors.GreenAccent
            ),
            ItemDetailTopBarPreview(
                isLoading = false,
                color = PassColors.YellowAccent
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassColors.PurpleAccent
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassColors.GreenAccent
            ),
            ItemDetailTopBarPreview(
                isLoading = true,
                color = PassColors.YellowAccent
            )
        )
}

data class ItemDetailTopBarPreview(
    val isLoading: Boolean,
    val color: Color
)
