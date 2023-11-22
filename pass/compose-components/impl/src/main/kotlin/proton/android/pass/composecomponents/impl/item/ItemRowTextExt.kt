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
import proton.android.pass.common.api.MatchSpan
import proton.android.pass.common.api.StringMatcher

private const val CHARACTER_OFFSET = 10
private const val MAX_RESULTS = 1

fun String.highlight(
    query: String,
    highlightColor: Color = Color.Unspecified
): AnnotatedString? {
    val matches = StringMatcher.match(this, query).take(MAX_RESULTS)
    if (matches.isEmpty()) return null

    val annotated = buildHighlightedString(this, matches, highlightColor)
    val offset = matches.first().start - CHARACTER_OFFSET
    return if (shouldAddVisualOffset(offset)) {
        AnnotatedString(Typography.ellipsis.toString()) + annotated
    } else {
        annotated
    }
}

private fun buildHighlightedString(
    input: String,
    matchIndexes: List<MatchSpan>,
    highlightColor: Color
) = buildAnnotatedString {
    matchIndexes.forEachIndexed { index, span ->
        // Get CHARACTER_OFFSET characters before the span start
        val startSubstring = input.substring(
            startIndex = (span.start - CHARACTER_OFFSET).coerceAtLeast(0),
            endIndex = span.end
        )
        append(startSubstring)

        // Append the highlighted word
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
        ) {
            append(input.substring(span.start, span.end + 1))
        }

        // Get CHARACTER_OFFSET characters after the span end
        val endIndex = (span.end + CHARACTER_OFFSET).coerceAtMost(input.length)
        val endSubstring = input.substring(
            startIndex = span.end + 1,
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
