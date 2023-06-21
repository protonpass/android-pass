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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ThemedHiddenStatePreviewProvider
import proton.android.pass.featureitemcreate.impl.login.PASSWORD_CONCEALED_LENGTH
import proton.pass.domain.HiddenState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun CardCVVInput(
    modifier: Modifier = Modifier,
    value: HiddenState,
    enabled: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val (text, visualTransformation) = when (value) {
        is HiddenState.Concealed -> "x".repeat(PASSWORD_CONCEALED_LENGTH) to PasswordVisualTransformation()
        is HiddenState.Revealed -> value.clearText to VisualTransformation.None
        is HiddenState.Empty -> "" to VisualTransformation.None
    }
    ProtonTextField(
        modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = text,
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        editable = enabled,
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_card_cvv_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_card_cvv_hint)) },
        visualTransformation = visualTransformation,
        leadingIcon = {
            Icon(
                painter = painterResource(CompR.drawable.ic_verified),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        },
        onFocusChange = onFocusChange
    )
}

@Preview
@Composable
fun CardCVVInputPreview(
    @PreviewParameter(ThemedHiddenStatePreviewProvider::class) input: Pair<Boolean, HiddenState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CardCVVInput(
                value = input.second,
                enabled = true,
                onChange = {},
                onFocusChange = {}
            )
        }
    }
}
