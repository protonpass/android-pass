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

fun String.highlight(
    query: String,
    highlightColor: Color = Color.Unspecified
): AnnotatedString? {
    val matches = StringMatcher.match(this, query)
    return if (matches.isEmpty()) {
        null
    } else {
        buildHighlightedString(this, matches, highlightColor)
    }
}

private fun buildHighlightedString(
    input: String,
    matchSpans: List<MatchSpan>,
    highlightColor: Color
) = buildAnnotatedString {
    val firstSpan = matchSpans.first()

    val firstSection = firstSection(input, firstSpan)
    val mainSection = mainSection(input, firstSpan, highlightColor)
    val endSection = endSection(input, matchSpans, highlightColor)

    append(firstSection)
    append(mainSection)
    append(endSection)
}

private fun mainSection(
    input: String,
    span: MatchSpan,
    highlightColor: Color
) = buildAnnotatedString {
    withStyle(
        style = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = highlightColor
        )
    ) {
        append(input.substring(span.start, span.end))
    }
}

private fun endSection(
    input: String,
    spans: List<MatchSpan>,
    highlightColor: Color
) = buildAnnotatedString {
    val firstSpan = spans.first()

    // Check if we need to add text after the string
    if (firstSpan.end >= input.length) return@buildAnnotatedString

    val endSubstringStart = firstSpan.end
    val endIndex = (firstSpan.end + CHARACTER_OFFSET).coerceAtMost(input.length)

    val endSubstring = input.substring(
        startIndex = endSubstringStart,
        endIndex = endIndex
    )

    // Check if there is any match that contained in the end substring
    val endSubstringMatches = spans.take(CHARACTER_OFFSET).filter {
        it.start >= endSubstringStart && it.end <= endIndex
    }.map {
        // Adapt it to the end substring index
        MatchSpan(
            start = (it.start - endSubstringStart).coerceAtLeast(0),
            end = (it.end - endSubstringStart).coerceAtLeast(0)
        )
    }

    endSubstring.forEachIndexed { index, c ->
        val hasSpan = endSubstringMatches.any { index >= it.start && index < it.end }
        if (hasSpan) {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = highlightColor
                )
            ) {
                append(c)
            }
        } else {
            append(c)
        }
    }

    if (endIndex < input.length) {
        append(Typography.ellipsis)
    }

}
private fun firstSection(input: String, span: MatchSpan) = buildAnnotatedString {
    // Check if we need to add text before the string
    // If the first span start is greater than CHARACTER_OFFSET, we need to add text before
    if (span.start == 0) {
        // Nothing needs to be added
    } else if (span.start < CHARACTER_OFFSET) {
        // Add the first part of the string until the span
        append(input.substring(0, span.start))
    } else {
        // Add the first part of the string until the span
        append(Typography.ellipsis)
        append(input.substring(span.start - CHARACTER_OFFSET, span.start))
    }
}
