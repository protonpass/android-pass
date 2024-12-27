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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.itemcreate.R

@Composable
internal fun CardExpirationDateInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    hasError: Boolean,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.none,
            top = Spacing.medium,
            end = Spacing.extraSmall,
            bottom = Spacing.medium
        ),
        value = convert(value),
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = hasError,
        errorMessage = stringResource(id = R.string.field_card_expiration_date_error),
        visualTransformation = { text ->
            if (text.length <= 2) {
                TransformedText(text, OffsetMapping.Identity)
            } else {
                val part1 = text.substring(0, 2)
                val part2 = text.substring(2, text.length)
                TransformedText(AnnotatedString("$part1 / $part2"), DateOffsetMapping)
            }
        },
        editable = enabled,
        label = {
            ProtonTextFieldLabel(
                text = stringResource(id = R.string.field_card_expiration_date_title),
                isError = hasError
            )
        },
        placeholder = {
            ProtonTextFieldPlaceHolder(
                text = stringResource(id = R.string.field_card_expiration_date_hint)
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_calendar_day),
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

private fun convert(value: String): String {
    val split = value.split("-")
    val join = if (split.size == 2) {
        split[1] + split[0]
    } else {
        value
    }
    return join
}


private val DateOffsetMapping: OffsetMapping = object : OffsetMapping {
    private val OFFSET_SPACES = 3
    override fun originalToTransformed(offset: Int): Int = if (offset <= 2) {
        offset
    } else {
        offset + OFFSET_SPACES
    }

    override fun transformedToOriginal(offset: Int): Int = if (offset <= 2) {
        offset
    } else {
        offset - OFFSET_SPACES
    }
}

@Preview
@Composable
fun CardExpirationDateInputPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CardExpirationDateInput(
                value = "1248",
                enabled = true,
                hasError = input.second,
                onChange = {}
            )
        }
    }
}
