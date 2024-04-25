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

package proton.android.pass.features.security.center.report.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportState
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar

@Composable
internal fun SecurityCenterReportContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterReportState,
    onUiEvent: (SecurityCenterReportUiEvent) -> Unit
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium - Spacing.extraSmall),
                onUpClick = { onUiEvent(SecurityCenterReportUiEvent.Back) },
                actions = {
                    CircleIconButton(
                        modifier = modifier,
                        iconPainter = painterResource(id = R.drawable.ic_proton_three_dots_vertical),
                        size = 40,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        tintColor = PassTheme.colors.interactionNormMajor2,
                        onClick = { onUiEvent(SecurityCenterReportUiEvent.OnMenuClick) }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .background(PassTheme.colors.backgroundStrong),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            ReportHeader(breachCount = breachCount, email = breachEmail)

            if (hasUnresolvedBreaches) {
                ResolveButton(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    emailId = unresolvedBreachEmails.first().emailId,
                    isLoading = isResolveLoading,
                    onUiEvent = onUiEvent
                )
            }

            if (isContentLoading) {
                Loading(modifier = Modifier.fillMaxSize())
            } else {
                SecurityCenterReportList(
                    modifier = Modifier.fillMaxWidth(),
                    state = state,
                    onUiEvent = onUiEvent
                )
            }
        }
    }
}
