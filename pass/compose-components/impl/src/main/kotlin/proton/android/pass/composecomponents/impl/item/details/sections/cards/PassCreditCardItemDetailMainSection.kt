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
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailSecureFieldRow
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PassCreditCardItemDetailMainSection(
    modifier: Modifier = Modifier,
    cardholder: String,
    cardNumber: String,
    expirationDate: String,
    cvv: String,
    pin: String,
    itemColors: ProtonItemColors,
) {
    RoundedCornersColumn(modifier = modifier) {
        if (cardholder.isNotBlank()) {
            PassItemDetailFieldRow(
                icon = painterResource(CoreR.drawable.ic_proton_user),
                title = stringResource(R.string.item_details_credit_card_section_cardholder_title),
                subtitle = cardholder,
                itemColors = itemColors,
            )
        }

        if (cardNumber.isNotBlank()) {
            PassDivider()

            PassItemDetailSecureFieldRow(
                icon = painterResource(CoreR.drawable.ic_proton_credit_card),
                title = stringResource(R.string.item_details_credit_card_section_card_number_title),
                subtitle = cardNumber,
                itemColors = itemColors,
            )
        }

        if (expirationDate.isNotBlank()) {
            PassDivider()

            PassItemDetailFieldRow(
                icon = painterResource(CoreR.drawable.ic_proton_calendar_day),
                title = stringResource(R.string.item_details_credit_card_section_expiration_date_title),
                subtitle = expirationDate,
                itemColors = itemColors,
            )
        }

        if (cvv.isNotBlank()) {
            PassDivider()

            PassItemDetailSecureFieldRow(
                icon = painterResource(CompR.drawable.ic_verified),
                title = stringResource(R.string.item_details_credit_card_section_cvv_title),
                subtitle = cvv,
                itemColors = itemColors,
            )
        }

        if (pin.isNotBlank()) {
            PassDivider()

            PassItemDetailSecureFieldRow(
                icon = painterResource(CoreR.drawable.ic_proton_grid_3),
                title = stringResource(R.string.item_details_credit_card_section_pin_title),
                subtitle = pin,
                itemColors = itemColors,
            )
        }
    }
}
