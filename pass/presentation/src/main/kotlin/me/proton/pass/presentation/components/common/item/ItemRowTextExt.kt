package me.proton.pass.presentation.components.common.item

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val CHARACTER_OFFSET = 20

fun String.highlight(text: String?): AnnotatedString =
    if (text.isNullOrBlank()) {
        buildAnnotatedString { append(this@highlight) }
    } else {
        val indexes = text.toRegex(setOf(RegexOption.IGNORE_CASE))
            .findAll(this@highlight)
            .map { it.range }
            .toList()
        val annotated = buildAnnotatedString {
            if (indexes.isEmpty()) {
                append(this@highlight)
            } else {
                indexes.fold(0) { start, intRange ->
                    append(this@highlight.substring(start, intRange.first))
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(this@highlight.substring(intRange))
                    }
                    return@fold intRange.last + 1
                }
                if (this@highlight.length > indexes.last().last + 1) {
                    append(
                        this@highlight.substring(
                            indexes.last().last + 1,
                            this@highlight.length
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
