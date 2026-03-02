/*
 * Copyright (c) 2025-2026 Proton AG
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.toUrlAnnotatedString
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    textModifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    linkColor: Color = Color.Unspecified,
    minimizedMaxLines: Int = 10
) {
    if (text.isBlank()) return
    var isExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val annotatedText = remember(text, linkColor) {
        text.toUrlAnnotatedString(linkColor) { url ->
            BrowserUtils.openWebsite(context = context, website = url)
        }
    }

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
                text = annotatedText,
                maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
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

@[Preview Composable]
internal fun ExpandableTextPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ExpandableText(
                text = "Check out https://proton.me or https://www.proton.me for more info.\n\n" +
                    "This is a note with URLs like http://example.com/path?q=1",
                linkColor = PassTheme.colors.noteInteractionNormMajor2
            )
        }
    }
}
