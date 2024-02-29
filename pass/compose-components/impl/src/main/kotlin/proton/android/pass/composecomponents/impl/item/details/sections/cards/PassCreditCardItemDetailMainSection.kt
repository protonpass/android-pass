/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailMaskedFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

private const val HIDDEN_CVV_TEXT_LENGTH = 4
private const val HIDDEN_PIN_TEXT_LENGTH = 4

@Composable
internal fun PassCreditCardItemDetailMainSection(
    modifier: Modifier = Modifier,
    cardholder: String,
    cardNumber: String,
    expirationDate: String,
    cvv: HiddenState,
    pin: HiddenState,
    itemColors: ProtonItemColors,
    onSectionClick: (String, ItemDetailsFieldType.Plain) -> Unit,
    onHiddenSectionClick: (HiddenState, ItemDetailsFieldType.Hidden) -> Unit,
    onHiddenSectionToggle: (Boolean, HiddenState, ItemDetailsFieldType.Hidden) -> Unit,
) {

    val sections = mutableListOf<@Composable (() -> Unit)?>()

    sections.add {
        PassItemDetailFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_user),
            title = stringResource(R.string.item_details_credit_card_section_cardholder_title),
            subtitle = cardholder,
            itemColors = itemColors,
            onClick = { onSectionClick(cardholder, ItemDetailsFieldType.Plain.Username) },
        ).takeIf { cardholder.isNotBlank() }
    }

    sections.add {
        PassItemDetailMaskedFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_credit_card),
            title = stringResource(R.string.item_details_credit_card_section_card_number_title),
            maskedSubtitle = TextMask.CardNumber(cardNumber),
            itemColors = itemColors,
            isToggleable = true,
            onClick = { onSectionClick(cardNumber, ItemDetailsFieldType.Plain.CardNumber) }
        ).takeIf { cardNumber.isNotBlank() }
    }

    sections.add {
        PassItemDetailMaskedFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_calendar_day),
            title = stringResource(R.string.item_details_credit_card_section_expiration_date_title),
            maskedSubtitle = TextMask.ExpirationDate(expirationDate),
            itemColors = itemColors,
        ).takeIf { expirationDate.isNotBlank() }
    }

    sections.add {
        PassItemDetailsHiddenFieldRow(
            icon = painterResource(CompR.drawable.ic_verified),
            title = stringResource(R.string.item_details_credit_card_section_cvv_title),
            hiddenState = cvv,
            hiddenTextLength = HIDDEN_CVV_TEXT_LENGTH,
            itemColors = itemColors,
            onClick = { onHiddenSectionClick(cvv, ItemDetailsFieldType.Hidden.Cvv) },
            onToggle = { isVisible ->
                onHiddenSectionToggle(isVisible, cvv, ItemDetailsFieldType.Hidden.Cvv)
            },
        ).takeIf { cvv !is HiddenState.Empty }
    }

    sections.add {
        PassItemDetailsHiddenFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_grid_3),
            title = stringResource(R.string.item_details_credit_card_section_pin_title),
            hiddenState = pin,
            hiddenTextLength = HIDDEN_PIN_TEXT_LENGTH,
            itemColors = itemColors,
            onToggle = { isVisible ->
                onHiddenSectionToggle(isVisible, pin, ItemDetailsFieldType.Hidden.Pin)
            },
        ).takeIf { pin !is HiddenState.Empty }
    }

    RoundedCornersColumn(modifier = modifier) {
        sections
            .filterNotNull()
            .forEachIndexed { index, block ->
                block()

                if (index < sections.lastIndex) {
                    PassDivider()
                }
            }
    }
}
