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
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.pass.domain.ItemContents

@Composable
fun CardDetails(
    modifier: Modifier = Modifier,
    content: ItemContents.CreditCard,
    onNameChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onCVVChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        CardHolderNameInput(value = content.cardHolder, onChange = onNameChanged)
        Divider(color = PassTheme.colors.inputBorderNorm)
        CardNumberInput(value = content.number, onChange = onNumberChanged)
        Divider(color = PassTheme.colors.inputBorderNorm)
        CardCVVInput(value = content.cvv, onChange = onCVVChanged)
        Divider(color = PassTheme.colors.inputBorderNorm)
        CardExpirationDateInput(value = content.expirationDate, onChange = onExpirationDateChanged)
    }
}
