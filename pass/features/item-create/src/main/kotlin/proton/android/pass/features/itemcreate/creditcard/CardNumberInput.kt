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
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.itemcreate.R

@Composable
internal fun CardNumberInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.none,
            top = Spacing.medium,
            end = Spacing.extraSmall,
            bottom = Spacing.medium
        ),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        editable = enabled,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Number),
        visualTransformation = { text -> cardNumberTransformedText(text) },
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

fun cardNumberTransformedText(text: AnnotatedString): TransformedText = if (text.isEmpty()) {
    TransformedText(text, OffsetMapping.Identity)
} else {
    val cardNumberSplits = text.chunked(4)
    val cardNumberWithSpaces = cardNumberSplits.joinToString(" ")

    TransformedText(
        text = AnnotatedString(cardNumberWithSpaces),
        offsetMapping = CardNumberOffsetMapping(cardNumberSplits)
    )
}


class CardNumberOffsetMapping(private val cardNumberGroups: List<String>) : OffsetMapping {
    private val maxTextSize = cardNumberGroups
        // Sum of all the card number groups
        .sumOf { it.length }

        // Number of spaces between the groups
        .plus(cardNumberGroups.size - 1)

    private val safeIntRange = IntRange(start = 0, endInclusive = maxTextSize)

    override fun originalToTransformed(offset: Int): Int {
        val splitIndex = splitIndex(offset)
        return (offset + splitIndex).coerceIn(safeIntRange)
    }

    override fun transformedToOriginal(offset: Int): Int {
        val splitIndex = splitIndex(offset)
        return if (splitIndex == 0) {
            offset
        } else {
            offset - splitIndex + 1
        }.coerceIn(safeIntRange)
    }

    private fun splitIndex(offset: Int): Int {
        var splitIndex = 0
        var splitOffset = 0
        for (group in cardNumberGroups) {
            if (offset < group.length + splitOffset) {
                break
            }
            splitOffset += group.length
            splitIndex++
        }
        return splitIndex
    }
}

@Preview
@Composable
fun CardNumberInputPreview(@PreviewParameter(ThemePreviewProvider::class) input: Boolean) {
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
