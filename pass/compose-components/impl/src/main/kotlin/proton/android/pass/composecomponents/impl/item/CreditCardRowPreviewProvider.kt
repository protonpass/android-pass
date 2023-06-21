package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.CreditCardType
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

class CreditCardRowPreviewProvider : PreviewParameterProvider<CreditCardRowParameter> {
    override val values: Sequence<CreditCardRowParameter>
        get() = sequenceOf(
            with(
                title = "A credit card",
                note = "Example credit card",
                number = "0000000000000000"
            ),
            with(
                title = "A credit card",
                note = "Example credit card",
                number = "0000000000000000",
                highlight = "credit"
            ),
            with(
                title = "A credit card",
                note = "some credit card note",
                cardHolder = "some cardholder",
                number = "0000000000000000",
                highlight = "some"
            ),
            with(
                title = "An empty credit card",
                note = "",
                cardHolder = "",
                number = "",
                highlight = ""
            )
        )

    companion object {
        private fun with(
            title: String,
            note: String = "",
            number: String,
            cardHolder: String = "",
            highlight: String = ""
        ) = CreditCardRowParameter(
            model = ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("456"),
                contents = ItemContents.CreditCard(
                    title = title,
                    note = note,
                    number = number,
                    cardHolder = cardHolder,
                    expirationDate = "2030-01",
                    pin = HiddenState.Concealed(""),
                    cvv = HiddenState.Concealed(""),
                    type = CreditCardType.Visa
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now()
            ),
            highlight = highlight
        )
    }
}

data class CreditCardRowParameter(
    val model: ItemUiModel,
    val highlight: String
)
