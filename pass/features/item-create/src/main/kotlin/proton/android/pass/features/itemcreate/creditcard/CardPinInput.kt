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

package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ThemedHiddenStatePreviewProvider
import proton.android.pass.features.itemcreate.common.UIHiddenState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun CardPinInput(
    modifier: Modifier = Modifier,
    value: UIHiddenState,
    enabled: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    HiddenNumberInput(
        modifier = modifier,
        value = value,
        enabled = enabled,
        label = stringResource(id = R.string.field_card_pin_title),
        placeholder = stringResource(id = R.string.field_card_pin_hint),
        icon = CoreR.drawable.ic_proton_grid_3,
        onChange = onChange,
        onFocusChange = onFocusChange
    )
}

@Preview
@Composable
fun CardPinInputPreview(
    @PreviewParameter(ThemedHiddenStatePreviewProvider::class) input: Pair<Boolean, UIHiddenState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CardPinInput(
                value = input.second,
                enabled = true,
                onChange = {},
                onFocusChange = {}
            )
        }
    }
}
