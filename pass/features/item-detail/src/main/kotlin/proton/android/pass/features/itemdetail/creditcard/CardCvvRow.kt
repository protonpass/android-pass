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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.domain.HiddenState
import proton.android.pass.features.itemdetail.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun CardCvvRow(
    modifier: Modifier = Modifier,
    cvv: HiddenState,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    CardHiddenRow(
        modifier = modifier,
        label = stringResource(R.string.credit_card_cvv_field_name),
        value = cvv,
        icon = CompR.drawable.ic_verified,
        onToggle = onToggle,
        onClick = onClick
    )
}

@Preview
@Composable
fun CardCvvRowPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val cvv = if (input.second) HiddenState.Revealed("", "1234") else HiddenState.Concealed("")
    PassTheme(isDark = input.first) {
        Surface {
            CardCvvRow(
                cvv = cvv,
                onToggle = {},
                onClick = {}
            )
        }
    }
}
