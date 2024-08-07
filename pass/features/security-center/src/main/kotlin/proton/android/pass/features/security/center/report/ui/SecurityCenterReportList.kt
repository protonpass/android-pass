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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportState
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterLoginItemRow

@[Composable OptIn(ExperimentalFoundationApi::class)]
internal fun SecurityCenterReportList(
    modifier: Modifier = Modifier,
    state: SecurityCenterReportState,
    onUiEvent: (SecurityCenterReportUiEvent) -> Unit
) = with(state) {
    LazyColumn(modifier = modifier) {
        if (hasUnresolvedBreaches) {
            stickyHeader {
                SecurityCenterReportListHeader(
                    titleResId = R.string.security_center_email_report_breaches_header
                )
            }

            items(
                items = unresolvedBreachEmails,
                key = { breach -> breach.emailId.id.id }
            ) { breach ->
                SecurityCenterReportBreachRow(
                    breach = breach,
                    onUiEvent = onUiEvent
                )
            }
        }

        if (hasResolvedBreaches) {
            stickyHeader {
                SecurityCenterReportListHeader(
                    titleResId = R.string.security_center_email_report_resolved_breaches_header
                )
            }

            items(
                items = resolvedBreachEmails,
                key = { breach -> breach.emailId.id.id }
            ) { breach ->
                SecurityCenterReportBreachRow(
                    breach = breach,
                    onUiEvent = onUiEvent
                )
            }
        }

        if (hasBeenUsedInLoginItems) {
            stickyHeader {
                SecurityCenterReportListHeader(
                    titleResId = R.string.security_center_email_report_used_in_header
                )
            }

            items(
                items = usedInLoginItems,
                key = { it.key }
            ) { itemUiModel ->
                SecurityCenterLoginItemRow(
                    itemUiModel = itemUiModel,
                    canLoadExternalImages = canLoadExternalImages,
                    shareIcon = getShareIcon(itemUiModel.shareId),
                    onClick = {
                        onUiEvent(
                            SecurityCenterReportUiEvent.OnItemClick(
                                shareId = itemUiModel.shareId,
                                itemId = itemUiModel.id
                            )
                        )
                    }
                )
            }
        }
    }
}
