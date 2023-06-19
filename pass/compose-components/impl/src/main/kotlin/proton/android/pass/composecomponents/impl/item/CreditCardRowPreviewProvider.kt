package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonuimodels.api.ItemUiModel

class CreditCardRowPreviewProvider : PreviewParameterProvider<CreditCardRowParameter> {
    override val values: Sequence<CreditCardRowParameter>
        get() = sequence {

        }
}

data class CreditCardRowParameter(
    val model: ItemUiModel,
    val highlight: String
)
