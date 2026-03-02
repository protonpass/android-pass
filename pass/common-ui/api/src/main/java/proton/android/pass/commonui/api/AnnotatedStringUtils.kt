/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.commonui.api

import android.util.Patterns
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

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

internal fun String.ensureUrlScheme(): String = when {
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true) -> this
    else -> "https://$this"
}

internal fun String.lowercaseSchemeAndHost(): String {
    val schemeEnd = indexOf("://")
    if (schemeEnd == -1) return this
    val authorityStart = schemeEnd + 3
    val pathStart = indexOf('/', authorityStart).takeIf { it != -1 } ?: length
    return substring(0, pathStart).lowercase() + substring(pathStart)
}

internal fun String.isPartOfEmail(matchStart: Int): Boolean = matchStart > 0 && this[matchStart - 1] == '@'

fun String.toUrlAnnotatedString(linkColor: Color, onUrlClick: (String) -> Unit): AnnotatedString {
    val matches = Patterns.WEB_URL
        .toRegex()
        .findAll(this)
        .filterNot { isPartOfEmail(it.range.first) }
        .toList()
    if (matches.isEmpty()) return AnnotatedString(this)

    return buildAnnotatedString {
        var lastIndex = 0

        matches.forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val url = matchResult.value

            append(this@toUrlAnnotatedString.substring(lastIndex, start))

            val fullUrl = url.ensureUrlScheme().lowercaseSchemeAndHost()

            withLink(
                LinkAnnotation.Url(
                    url = fullUrl,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = {
                        onUrlClick(fullUrl)
                    }
                )
            ) {
                append(url)
            }
            lastIndex = end
        }
        if (lastIndex < this@toUrlAnnotatedString.length) {
            append(this@toUrlAnnotatedString.substring(lastIndex))
        }
    }
}
