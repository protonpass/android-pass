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

package proton.android.pass.features.security.center.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.home.presentation.SecurityCenterHomeDarkWebMonitoring
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterCounterRow
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterCounterRowModel

@[Composable Suppress("FunctionMaxLength")]
internal fun SecurityCenterHomeDarkWebMonitoringSection(
    modifier: Modifier = Modifier,
    onUiEvent: (SecurityCenterHomeUiEvent) -> Unit,
    darkWebMonitoring: SecurityCenterHomeDarkWebMonitoring
) {
    val freeDataBreachesVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.FreeDataBreaches)
    }
    val freeNoDataBreachesVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.FreeNoDataBreaches)
    }
    val paidDataBreachesVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.PaidDataBreaches)
    }
    val paidNoDataBreachesVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.PaidNoDataBreaches)
    }
    val freeLoadingVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.FreeLoading)
    }
    val paidLoadingVisible by remember(darkWebMonitoring) {
        mutableStateOf(darkWebMonitoring is SecurityCenterHomeDarkWebMonitoring.PaidLoading)
    }

    Box(modifier = modifier) {
        AnimatedVisibility(freeDataBreachesVisible, enter = fadeIn(), exit = fadeOut()) {
            val state = darkWebMonitoring as SecurityCenterHomeDarkWebMonitoring.FreeDataBreaches
            SecurityCenterHomeDataBreachesWidget(
                dataBreachedSite = state.dataBreachedSite,
                dataBreachedTime = state.dataBreachedTime,
                dataBreachedEmail = state.dateBreachedEmail,
                dataBreachedPassword = state.dataBreachedPassword,
                onActionClick = {
                    onUiEvent(SecurityCenterHomeUiEvent.OnUpsell(PaidFeature.DarkWebMonitoring))
                }
            )
        }

        AnimatedVisibility(freeNoDataBreachesVisible, enter = fadeIn(), exit = fadeOut()) {
            SecurityCenterHomeNoDataBreachesWidget(
                onActionClick = {
                    onUiEvent(SecurityCenterHomeUiEvent.OnUpsell(PaidFeature.DarkWebMonitoring))
                }
            )
        }

        AnimatedVisibility(freeLoadingVisible, enter = fadeIn(), exit = fadeOut()) {
            SecurityCenterHomeLoadingBreachesWidget()
        }

        AnimatedVisibility(paidDataBreachesVisible, enter = fadeIn(), exit = fadeOut()) {
            val state = darkWebMonitoring as SecurityCenterHomeDarkWebMonitoring.PaidDataBreaches
            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Alert(
                    title = stringResource(id = R.string.security_center_home_row_data_breaches_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_data_breaches_subtitle),
                    count = state.dataBreachesCount
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowDataBreaches) }
            )
        }

        AnimatedVisibility(paidNoDataBreachesVisible, enter = fadeIn(), exit = fadeOut()) {
            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Success(
                    title = stringResource(id = R.string.security_center_home_dark_web_monitoring_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_no_data_breaches_subtitle)
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowDataBreaches) }
            )
        }

        AnimatedVisibility(paidLoadingVisible, enter = fadeIn(), exit = fadeOut()) {
            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Loading(
                    title = stringResource(id = R.string.security_center_home_dark_web_monitoring_title)
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowDataBreaches) }
            )
        }
    }
}
