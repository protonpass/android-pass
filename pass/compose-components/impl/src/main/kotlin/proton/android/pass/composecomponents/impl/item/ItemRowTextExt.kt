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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val CHARACTER_OFFSET = 20

fun String.highlight(matches: Sequence<MatchResult>): AnnotatedString {
    val indexes = matches.map { it.range }.toList()
    val annotated = buildHighlightedString(this, indexes)
    val offset = indexes.first().first - CHARACTER_OFFSET
    return if (shouldAddVisualOffset(offset)) {
        AnnotatedString(Typography.ellipsis.toString()) +
            annotated.subSequence(offset, length)
    } else {
        annotated
    }
}

private fun buildHighlightedString(input: String, indexes: List<IntRange>) =
    buildAnnotatedString {
        indexes.fold(0) { start, intRange ->
            append(input.substring(start, intRange.first))
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(input.substring(intRange))
            }
            return@fold intRange.last + 1
        }
        if (input.length > indexes.last().last + 1) {
            append(
                input.substring(
                    indexes.last().last + 1,
                    input.length
                )
            )
        }
    }

private fun shouldAddVisualOffset(offset: Int) = offset > 0
