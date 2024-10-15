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

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

@Composable
fun PassTextWithInnerStyle(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle,
    innerText: String,
    innerStyle: TextStyle,
    textAlign: TextAlign = TextAlign.Start
) {
    val innerStartIndex = remember(text, innerText) { text.indexOf(innerText) }
    val innerEndIndex = remember(text, innerText) { innerStartIndex + innerText.length }

    buildAnnotatedString {
        withStyle(style = ParagraphStyle(textAlign = textAlign)) {
            withStyle(style = textStyle.toSpanStyle()) {
                append(text)
            }
        }

        addStyle(
            style = innerStyle.toSpanStyle(),
            start = innerStartIndex, end = innerEndIndex
        )
    }.also { annotatedText ->
        Text(
            modifier = modifier,
            text = annotatedText
        )
    }
}
