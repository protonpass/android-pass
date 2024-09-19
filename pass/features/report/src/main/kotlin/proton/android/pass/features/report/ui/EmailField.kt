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

package proton.android.pass.features.report.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.features.report.R
import proton.android.pass.features.report.presentation.EmailBlank
import proton.android.pass.features.report.presentation.EmailError
import proton.android.pass.features.report.presentation.EmailInvalid

@Composable
fun EmailField(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    error: EmailError?,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(Spacing.medium),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        editable = enabled,
        isError = error != null,
        errorMessage = when (error) {
            EmailBlank -> stringResource(R.string.email_cannot_be_blank)
            EmailInvalid -> stringResource(R.string.email_invalid)
            else -> ""
        },
        label = { ProtonTextFieldLabel(text = stringResource(R.string.email_field_label)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(R.string.email_field_hint)) }
    )
}
