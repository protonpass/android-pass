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
import me.proton.core.presentation.R as CoreR

@Composable
fun CardPinRow(
    modifier: Modifier = Modifier,
    pin: HiddenState,
    onToggle: () -> Unit
) {
    CardHiddenRow(
        modifier = modifier,
        label = stringResource(R.string.credit_card_pin_field_name),
        value = pin,
        icon = CoreR.drawable.ic_proton_grid_3,
        onToggle = onToggle,
        onClick = null
    )
}

@Preview
@Composable
fun CardPinRowPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val pin = if (input.second) HiddenState.Revealed("", "1234") else HiddenState.Concealed("")
    PassTheme(isDark = input.first) {
        Surface {
            CardPinRow(
                pin = pin,
                onToggle = {}
            )
        }
    }
}
