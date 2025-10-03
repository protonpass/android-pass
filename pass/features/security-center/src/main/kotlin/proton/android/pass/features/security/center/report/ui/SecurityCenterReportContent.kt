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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportState
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportUiEvent.OnMenuClick
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecurityCenterReportContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterReportState,
    onUiEvent: (SecurityCenterReportUiEvent) -> Unit,
    isDialogVisible: Boolean,
    isDialogLoading: Boolean
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium - Spacing.extraSmall),
                onUpClick = { onUiEvent(SecurityCenterReportUiEvent.Back) },
                actions = {
                    breachEmailId?.let { id ->
                        CircleIconButton(
                            drawableRes = CoreR.drawable.ic_proton_three_dots_vertical,
                            size = 40,
                            backgroundColor = PassTheme.colors.interactionNormMinor1,
                            tintColor = PassTheme.colors.interactionNormMajor2,
                            iconContentDescription = stringResource(
                                id = R.string.security_center_email_report_options_menu
                            ),
                            onClick = {
                                OnMenuClick(
                                    id = id,
                                    isMonitored = state.isBreachExcludedFromMonitoring
                                ).also(onUiEvent)
                            }
                        )
                    }
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

            AnimatedVisibility(state.isBreachExcludedFromMonitoring) {
                ExcludedTag()
            }

            if (hasUnresolvedBreaches) {
                PassCircleButton(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    text = stringResource(id = R.string.security_center_email_report_mark_as_resolved),
                    textColor = PassTheme.colors.interactionNormMajor2,
                    onClick = { onUiEvent(SecurityCenterReportUiEvent.OnMarkEmailBreachesAsResolved) }
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

    SecurityCenterReportResolveBreachDialog(
        isDialogVisible = isDialogVisible,
        isDialogLoading = isDialogLoading,
        onConfirm = {
            unresolvedBreachesEmailId?.let { emailId ->
                onUiEvent(SecurityCenterReportUiEvent.MarkAsResolvedClick(emailId))
            }
        },
        onDismiss = {
            onUiEvent(SecurityCenterReportUiEvent.OnMarkEmailBreachesAsResolvedCancelled)
        }
    )
}

