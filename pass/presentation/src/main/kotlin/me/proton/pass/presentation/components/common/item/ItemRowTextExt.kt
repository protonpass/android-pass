package me.proton.pass.presentation.components.common.item

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some

fun String.highlight(text: Option<String>): AnnotatedString =
    when (text) {
        None -> buildAnnotatedString { append(this@highlight) }
        is Some -> buildAnnotatedString {
            val indexes = text.value.toRegex(setOf(RegexOption.IGNORE_CASE))
                .findAll(this@highlight)
                .map { it.range }
                .toList()
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
                    append(this@highlight.substring(indexes.last().last + 1, this@highlight.length))
                }
            }
        }
    }
