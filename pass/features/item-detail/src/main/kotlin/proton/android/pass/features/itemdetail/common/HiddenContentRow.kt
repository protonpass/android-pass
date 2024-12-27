/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemdetail.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import me.proton.core.presentation.R as CoreR

@Composable
fun HiddenContentRow(
    modifier: Modifier = Modifier,
    sectionContent: AnnotatedString,
    label: String,
    textStyle: TextStyle = ProtonTheme.typography.defaultNorm,
    isContentVisible: Boolean,
    toggleIconBackground: Color,
    toggleIconForeground: Color,
    icon: @Composable () -> Unit,
    middleSection: @Composable () -> Unit = {},
    revealAction: String,
    concealAction: String,
    onToggleClick: () -> Unit,
    onRowClick: (() -> Unit)?
) {
    val (actionIcon, actionContent) = if (isContentVisible) {
        CoreR.drawable.ic_proton_eye_slash to concealAction
    } else {
        CoreR.drawable.ic_proton_eye to revealAction
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(
                condition = onRowClick != null,
                ifTrue = {
                    clickable(onClick = onRowClick!!)
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(text = label)
            Spacer(modifier = Modifier.height(8.dp))
            SectionSubtitle(text = sectionContent, textStyle = textStyle)
        }
        middleSection()
        Circle(
            backgroundColor = toggleIconBackground,
            onClick = onToggleClick
        ) {
            Icon(
                painter = painterResource(actionIcon),
                contentDescription = actionContent,
                tint = toggleIconForeground
            )
        }
    }
}

@Preview
@Composable
fun HiddenContentRowPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val text = if (input.second) "Content" else "****"
    PassTheme(isDark = input.first) {
        Surface {
            HiddenContentRow(
                sectionContent = AnnotatedString(text),
                label = "Some label",
                isContentVisible = input.second,
                toggleIconBackground = PassTheme.colors.loginInteractionNormMinor1,
                toggleIconForeground = PassTheme.colors.loginInteractionNormMajor2,
                icon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_key),
                        contentDescription = null,
                        tint = PassTheme.colors.loginInteractionNorm
                    )
                },
                revealAction = "",
                concealAction = "",
                onToggleClick = {},
                onRowClick = {}

            )
        }
    }
}
