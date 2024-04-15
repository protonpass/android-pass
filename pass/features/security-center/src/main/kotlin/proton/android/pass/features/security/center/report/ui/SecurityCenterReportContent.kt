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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportDestination
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportState
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterLoginItemRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SecurityCenterReportContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterReportState,
    onNavigate: (SecurityCenterReportDestination) -> Unit
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium - Spacing.extraSmall),
                onUpClick = { onNavigate(SecurityCenterReportDestination.Back) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .background(PassTheme.colors.backgroundStrong),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            ReportHeader(breachCount = breachCount, email = email)

            if (isLoading) {
                Loading()
            } else {
                LazyColumn(Modifier.fillMaxWidth()) {
                    if (hasBreachEmails) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = PassTheme.colors.backgroundStrong)
                                    .padding(horizontal = Spacing.medium),
                                text = stringResource(R.string.security_center_email_report_breaches_header),
                                style = ProtonTheme.typography.body1Medium
                            )
                        }
                        items(
                            items = breachEmails,
                            key = { breach -> breach.id }
                        ) { breach ->
                            BreachRow(breach = breach)
                        }
                    }

                    if (hasBeenUsedInItems) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = PassTheme.colors.backgroundStrong)
                                    .padding(horizontal = Spacing.medium),
                                text = stringResource(R.string.security_center_email_report_used_in_header),
                                style = ProtonTheme.typography.body1Medium
                            )
                        }

                        items(
                            items = usedInItems,
                            key = { itemUiModel -> itemUiModel.id.id }
                        ) { itemUiModel ->
                            SecurityCenterLoginItemRow(
                                itemUiModel = itemUiModel,
                                canLoadExternalImages = canLoadExternalImages,
                                onClick = {

                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
