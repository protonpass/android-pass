/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

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
                userId = UserId("user-id"),
                shareId = ShareId("456"),
                contents = ItemContents.CreditCard(
                    title = title,
                    note = note,
                    number = number,
                    cardHolder = cardHolder,
                    expirationDate = "2030-01",
                    pin = HiddenState.Concealed(""),
                    cvv = HiddenState.Concealed(""),
                    type = CreditCardType.Visa,
                    customFields = emptyList()
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = false,
                pinTime = Clock.System.now(),
                revision = 1,
                shareCount = 0,
                shareType = ShareType.Vault
            ),
            highlight = highlight
        )
    }
}

data class CreditCardRowParameter(
    val model: ItemUiModel,
    val highlight: String
)
