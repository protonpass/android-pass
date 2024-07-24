/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import proton.android.pass.commonui.api.PassTheme

private const val ANNOTATION = ""
private const val ANNOTATION_TAG = "PassTextWithInnerLink"

@Composable
fun PassTextWithInnerLink(
    modifier: Modifier = Modifier,
    text: String,
    innerLink: String,
    onClick: () -> Unit,
    style: TextStyle = TextStyle.Default
) {
    val clickableStartIndex = text.indexOf(innerLink)

    buildAnnotatedString {
        append(text)

        if (clickableStartIndex != -1) {
            val clickableEndIndex = clickableStartIndex.plus(innerLink.length)

            addStyle(
                style = SpanStyle(
                    color = PassTheme.colors.interactionNormMajor2,
                    textDecoration = TextDecoration.Underline
                ),
                start = clickableStartIndex,
                end = clickableEndIndex
            )

            addStringAnnotation(
                tag = ANNOTATION_TAG,
                annotation = ANNOTATION,
                start = clickableStartIndex,
                end = clickableEndIndex
            )
        }
    }.let { annotatedString ->
        ClickableText(
            modifier = modifier,
            text = annotatedString,
            style = style,
            onClick = { offset ->
                annotatedString
                    .getStringAnnotations(tag = ANNOTATION_TAG, start = offset, end = offset)
                    .firstOrNull()
                    ?.also { onClick() }
            }
        )
    }
}
