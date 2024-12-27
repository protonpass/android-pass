/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemdetail.creditcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.HiddenState

@Composable
fun MainCreditCardSection(
    modifier: Modifier = Modifier,
    cardHolder: String,
    number: CardNumberState,
    pin: HiddenState,
    cvv: HiddenState,
    expirationDate: String,
    isDowngradedMode: Boolean,
    onEvent: (CreditCardDetailEvent) -> Unit
) {
    if (!canShowSection(cardHolder, number, pin, cvv, expirationDate)) return

    val sections = mutableListOf<@Composable () -> Unit>()
    if (cardHolder.isNotBlank()) {
        sections += {
            CardHolderRow(
                name = cardHolder,
                onClick = { onEvent(CreditCardDetailEvent.OnCardHolderClick) }
            )
        }
    }
    if (number.hasContent()) {
        sections += {
            CardNumberRow(
                number = number,
                isDowngradedMode = isDowngradedMode,
                onToggle = { onEvent(CreditCardDetailEvent.OnToggleNumberClick) },
                onClick = { onEvent(CreditCardDetailEvent.OnNumberClick) },
                onUpgradeClick = { onEvent(CreditCardDetailEvent.OnUpgradeClick) }
            )
        }
    }

    if (expirationDate.isNotBlank()) {
        sections += {
            CardExpirationDateRow(expirationDate = expirationDate)
        }
    }

    if (cvv !is HiddenState.Empty) {
        sections += {
            CardCvvRow(
                cvv = cvv,
                onToggle = { onEvent(CreditCardDetailEvent.OnToggleCvvClick) },
                onClick = { onEvent(CreditCardDetailEvent.OnCvvClick) }
            )
        }
    }

    if (pin !is HiddenState.Empty) {
        sections += {
            CardPinRow(
                pin = pin,
                onToggle = { onEvent(CreditCardDetailEvent.OnTogglePinClick) }
            )
        }
    }

    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        sections.forEachIndexed { idx, func ->
            func()
            if (idx < sections.lastIndex) {
                PassDivider()
            }
        }
    }

}

@Suppress("ComplexCondition")
private fun canShowSection(
    cardHolder: String,
    number: CardNumberState,
    pin: HiddenState,
    cvv: HiddenState,
    expirationDate: String
): Boolean {
    return !(
        cardHolder.isBlank() &&
            !number.hasContent() &&
            pin is HiddenState.Empty &&
            cvv is HiddenState.Empty &&
            expirationDate.isBlank()
        )
}

