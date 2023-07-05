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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun CardNumberInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        editable = enabled,
        textStyle = ProtonTheme.typography.defaultNorm,
        keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Number),
        visualTransformation = { text ->
            if (text.isEmpty())
                return@ProtonTextField TransformedText(text, OffsetMapping.Identity)
            val split = text.chunked(4)
            TransformedText(
                AnnotatedString(split.joinToString(" ")),
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = offset + split.size - 1

                    override fun transformedToOriginal(offset: Int): Int = offset - split.size + 1
                }
            )
        },
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_card_number_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_card_number_hint)) },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_credit_card),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        }
    )
}

@Preview
@Composable
fun CardNumberInputPreview(
    @PreviewParameter(ThemePreviewProvider::class) input: Boolean
) {
    PassTheme(isDark = input) {
        Surface {
            CardNumberInput(
                value = "1234567891234567",
                enabled = true,
                onChange = {}
            )
        }
    }
}
