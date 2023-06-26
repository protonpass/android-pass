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

package proton.android.pass.featureitemdetail.impl.creditcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.pass.domain.HiddenState

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
    if (!canShowSection(cardHolder, number, pin, cvv)) return

    val sections = mutableListOf<@Composable () -> Unit>()
    if (cardHolder.isNotBlank()) {
        sections += {
            CardHolderRow(
                name = cardHolder,
                isDowngradedMode = isDowngradedMode,
                onClick = { onEvent(CreditCardDetailEvent.OnCardHolderClick) },
                onUpgradeClick = { onEvent(CreditCardDetailEvent.OnUpgradeClick) }
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

    if (cvv !is HiddenState.Empty) {
        sections += {
            CardCvvRow(
                cvv = cvv,
                isDowngradedMode = isDowngradedMode,
                onToggle = { onEvent(CreditCardDetailEvent.OnToggleCvvClick) },
                onClick = { onEvent(CreditCardDetailEvent.OnCvvClick) },
                onUpgradeClick = { onEvent(CreditCardDetailEvent.OnUpgradeClick) }
            )
        }
    }

    if (pin !is HiddenState.Empty) {
        sections += {
            CardPinRow(
                pin = pin,
                isDowngradedMode = isDowngradedMode,
                onToggle = { onEvent(CreditCardDetailEvent.OnTogglePinClick) },
                onUpgradeClick = { onEvent(CreditCardDetailEvent.OnUpgradeClick) }
            )
        }
    }

    if (expirationDate.isNotBlank()) {
        sections += {
            CardExpirationDateRow(
                expirationDate = expirationDate,
                isDowngradedMode = isDowngradedMode,
                onUpgradeClick = { onEvent(CreditCardDetailEvent.OnUpgradeClick) }
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
    cvv: HiddenState
): Boolean {
    if (cardHolder.isBlank() &&
        !number.hasContent() &&
        pin is HiddenState.Empty &&
        cvv is HiddenState.Empty
    ) return false

    return true
}

