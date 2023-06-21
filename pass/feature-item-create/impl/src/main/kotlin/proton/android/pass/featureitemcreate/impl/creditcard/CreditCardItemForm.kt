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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnCVVChange
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnCVVFocusChange
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnExpirationDateChange
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnNameChange
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnNoteChange
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.OnNumberChange
import proton.pass.domain.ItemContents

@Composable
fun CreditCardItemForm(
    modifier: Modifier = Modifier,
    content: ItemContents.CreditCard,
    enabled: Boolean,
    titleSection: @Composable (ColumnScope.() -> Unit),
    onEvent: (CreditCardContentEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        titleSection()
        CardDetails(
            content = content,
            enabled = enabled,
            onNameChanged = { onEvent(OnNameChange(it)) },
            onNumberChanged = { onEvent(OnNumberChange(it)) },
            onCVVChanged = { onEvent(OnCVVChange(it)) },
            onExpirationDateChanged = { onEvent(OnExpirationDateChange(it)) },
            onCVVFocusChange = { onEvent(OnCVVFocusChange(it)) }
        )
        SimpleNoteSection(
            value = content.note,
            enabled = enabled,
            onChange = { onEvent(OnNoteChange(it)) }
        )
    }
}
