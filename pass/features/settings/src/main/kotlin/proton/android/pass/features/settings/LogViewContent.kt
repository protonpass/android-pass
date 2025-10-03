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

package proton.android.pass.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.overlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import me.proton.core.presentation.R as CoreR

@Composable
fun LogViewContent(
    modifier: Modifier = Modifier,
    content: String,
    onUpClick: () -> Unit,
    onShareLogsClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.view_logs_title),
                onUpClick = onUpClick,
                actions = {
                    Circle(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onShareLogsClick
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_arrow_up_from_square),
                            contentDescription = "",
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        SelectionContainer(
            Modifier
                .verticalScroll(rememberScrollState())
                .background(PassTheme.colors.backgroundStrong)
                .padding(contentPadding)
                .padding(Spacing.medium)
        ) {
            Text(
                text = content,
                style = ProtonTheme.typography.overlineNorm
            )
        }
    }
}
