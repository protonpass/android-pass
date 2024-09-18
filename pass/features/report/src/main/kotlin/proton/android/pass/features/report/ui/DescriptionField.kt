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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.features.report.R
import proton.android.pass.features.report.presentation.DescriptionBlank
import proton.android.pass.features.report.presentation.DescriptionError
import proton.android.pass.features.report.presentation.DescriptionTooLong
import proton.android.pass.features.report.presentation.DescriptionTooShort

@Composable
fun DescriptionField(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    error: DescriptionError?,
    onChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    ProtonTextField(
        modifier = modifier
            .defaultMinSize(minHeight = 120.dp)
            .focusRequester(focusRequester)
            .roundedContainerNorm()
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.Top,
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        singleLine = false,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        editable = enabled,
        isError = error != null,
        errorMessage = when (error) {
            DescriptionBlank -> stringResource(R.string.description_cannot_be_blank)
            DescriptionTooShort -> stringResource(R.string.description_is_too_short)
            DescriptionTooLong -> stringResource(R.string.description_is_too_long)
            else -> ""
        },
        label = { ProtonTextFieldLabel(text = stringResource(R.string.description_field_label)) },
        placeholder = {
            ProtonTextFieldPlaceHolder(
                text = stringResource(R.string.description_field_hint),
                maxLines = Int.MAX_VALUE
            )
        }
    )
    RequestFocusLaunchedEffect(focusRequester, true)
}
