package proton.android.pass.featurecreateitem.impl.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassColors

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassColors.PurpleAccent
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassColors.GreenAccent
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                color = PassColors.YellowAccent
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassColors.PurpleAccent
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassColors.GreenAccent
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                color = PassColors.YellowAccent
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val color: Color
)
