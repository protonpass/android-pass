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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val CHARACTER_OFFSET = 10
private const val MAX_RESULTS = 1

fun String.highlight(
    matches: Sequence<MatchResult>,
    highlightColor: Color = Color.Unspecified
): AnnotatedString {
    val indexes = matches.map { it.range }.toList().take(MAX_RESULTS)
    if (indexes.isEmpty()) return AnnotatedString(this)

    val annotated = buildHighlightedString(this, indexes, highlightColor)
    val offset = indexes.first().first - CHARACTER_OFFSET
    return if (shouldAddVisualOffset(offset)) {
        AnnotatedString(Typography.ellipsis.toString()) + annotated
    } else {
        annotated
    }
}

private fun buildHighlightedString(
    input: String,
    matchIndexes: List<IntRange>,
    highlightColor: Color
) = buildAnnotatedString {
    matchIndexes.forEachIndexed { index, span ->
        // Get CHARACTER_OFFSET characters before the span start
        val startSubstring = input.substring(
            startIndex = (span.first - CHARACTER_OFFSET).coerceAtLeast(0),
            endIndex = span.first
        )
        append(startSubstring)

        // Append the highlighted word
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
        ) {
            append(input.substring(span.first, span.last + 1))
        }

        // Get CHARACTER_OFFSET characters after the span end
        val endIndex = (span.last + CHARACTER_OFFSET).coerceAtMost(input.length)
        val endSubstring = input.substring(
            startIndex = span.last + 1,
            endIndex = endIndex
        )
        append(endSubstring)
        if (endIndex < input.length) {
            append(Typography.ellipsis)
        }

        if (index == 0 && matchIndexes.size > 1) {
            append("\n")
        }
    }

}

private fun shouldAddVisualOffset(offset: Int) = offset > 0
