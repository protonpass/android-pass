package me.proton.pass.presentation.components.common.item

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val CHARACTER_OFFSET = 20

fun String.highlight(highlight: String?): AnnotatedString {
    val input = this@highlight
    return if (highlight.isNullOrBlank()) {
        buildAnnotatedString { append(input) }
    } else {
        val indexes = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
            .findAll(input)
            .map { it.range }
            .toList()
        val annotated = buildAnnotatedString {
            if (indexes.isEmpty()) {
                append(input)
            } else {
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
        }
        if (indexes.isNotEmpty()) {
            val offset = indexes.first().first - CHARACTER_OFFSET
            if (offset > 0) {
                AnnotatedString(Typography.ellipsis.toString()) +
                    annotated.subSequence(offset, length)
            } else {
                annotated
            }
        } else {
            annotated
        }
    }
}
