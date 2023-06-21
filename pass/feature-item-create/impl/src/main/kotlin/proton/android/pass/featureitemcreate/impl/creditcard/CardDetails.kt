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

package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.PersistentSet
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.pass.domain.ItemContents

@Composable
fun CardDetails(
    modifier: Modifier = Modifier,
    content: ItemContents.CreditCard,
    enabled: Boolean,
    validationErrors: PersistentSet<CreditCardValidationErrors>,
    onNameChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onCVVChanged: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCVVFocusChange: (Boolean) -> Unit,
    onPinFocusChange: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        CardHolderNameInput(value = content.cardHolder, enabled = enabled, onChange = onNameChanged)
        PassDivider()
        CardNumberInput(value = content.number, enabled = enabled, onChange = onNumberChanged)
        PassDivider()
        CardCVVInput(
            value = content.cvv,
            enabled = enabled,
            onChange = onCVVChanged,
            onFocusChange = onCVVFocusChange
        )
        PassDivider()
        CardPinInput(
            value = content.pin,
            enabled = enabled,
            onChange = onPinChanged,
            onFocusChange = onPinFocusChange
        )
        PassDivider()
        CardExpirationDateInput(
            value = content.expirationDate,
            enabled = enabled,
            hasError = validationErrors.contains(CreditCardValidationErrors.InvalidExpirationDate),
            onChange = onExpirationDateChanged
        )
    }
}
