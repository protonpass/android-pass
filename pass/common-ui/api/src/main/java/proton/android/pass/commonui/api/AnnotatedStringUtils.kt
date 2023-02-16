package proton.android.pass.commonui.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

fun String.toPasswordAnnotatedString(
    digitColor: Color,
    symbolColor: Color,
    letterColor: Color
): AnnotatedString = this
    .map {
        val color = when {
            it.isDigit() -> digitColor
            !it.isLetterOrDigit() -> symbolColor
            else -> letterColor
        }
        AnnotatedString(it.toString(), SpanStyle(color))
    }
    .reduceOrNull { acc, next -> acc.plus(next) }
    ?: AnnotatedString("")

fun String.asAnnotatedString(): AnnotatedString = AnnotatedString(this)
