/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.notes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    textModifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    minimizedMaxLines: Int = 10
) {
    if (text.isBlank()) return
    var isExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = modifier
            .animateContentSize()
            .roundedContainer(
                backgroundColor = Color.Transparent,
                borderColor = ProtonTheme.colors.separatorNorm
            )
    ) {
        val isButtonShowing = isTextOverflowing || isExpanded
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.medium)
                .padding(horizontal = Spacing.medium)
                .applyIf(!isButtonShowing, ifTrue = { padding(bottom = Spacing.medium) })
        ) {
            Text(
                modifier = textModifier,
                text = text,
                maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
                    textLayoutResult = layoutResult
                    isTextOverflowing = layoutResult.hasVisualOverflow
                },
                style = ProtonTheme.typography.defaultNorm,
                color = textColor
            )
        }
        if (isButtonShowing) {
            TransparentTextButton(
                modifier = Modifier.align(Alignment.End),
                text = if (isExpanded) {
                    stringResource(R.string.collapse_expanded_text)
                } else {
                    stringResource(R.string.expand_collpased_text)
                },
                color = PassTheme.colors.noteInteractionNormMajor2,
                onClick = { isExpanded = !isExpanded }
            )
        }
    }
}
