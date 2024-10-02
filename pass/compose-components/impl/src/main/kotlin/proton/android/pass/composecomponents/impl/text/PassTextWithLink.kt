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

import androidx.annotation.StringRes
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

private const val LINK_ANNOTATION = ""
private const val LINK_ANNOTATION_TAG = "link_tag"

@Composable
fun PassTextWithLink(
    modifier: Modifier = Modifier,
    @StringRes textResId: Int,
    textStyle: TextStyle,
    @StringRes linkResId: Int,
    linkStyle: TextStyle,
    onLinkClick: (String) -> Unit,
    textAlign: TextAlign = TextAlign.Start,
    tag: String = LINK_ANNOTATION_TAG,
    annotation: String = LINK_ANNOTATION
) {
    val linkText = stringResource(id = linkResId)
    val text = stringResource(id = textResId, linkText)
    val linkStartIndex = remember { text.indexOf(linkText) }
    val linkEndIndex = remember { linkStartIndex + linkText.length }

    buildAnnotatedString {
        withStyle(style = ParagraphStyle(textAlign = textAlign)) {
            withStyle(style = textStyle.toSpanStyle()) {
                append(text)
            }
        }

        addStyle(
            style = linkStyle.toSpanStyle(),
            start = linkStartIndex, end = linkEndIndex
        )

        addStringAnnotation(
            tag = tag,
            annotation = annotation,
            start = linkStartIndex,
            end = linkEndIndex
        )
    }.also { annotatedText ->
        ClickableText(
            modifier = modifier,
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = tag,
                    start = offset,
                    end = offset
                )
                    .firstOrNull()
                    ?.let { annotation ->
                        onLinkClick(annotation.item)
                    }
            }
        )
    }
}
