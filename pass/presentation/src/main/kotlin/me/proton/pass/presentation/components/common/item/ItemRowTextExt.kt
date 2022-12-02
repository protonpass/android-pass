package me.proton.pass.presentation.components.common.item

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
