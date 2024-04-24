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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarEvent
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarSelection
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.bottombar.PassHomeBottomBar
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination
import proton.android.pass.features.security.center.home.presentation.SecurityCenterHomeState
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterCounterRow
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterCounterRowModel
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterRow
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterToggleRow

@Composable
internal fun SecurityCenterHomeContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecurityCenterHomeUiEvent) -> Unit,
    state: SecurityCenterHomeState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium),
                title = stringResource(R.string.security_center_home_top_bar_title)
            )
        },
        bottomBar = {
            PassHomeBottomBar(
                selection = HomeBottomBarSelection.SecurityCenter,
                onEvent = { homeBottomBarEvent ->
                    when (homeBottomBarEvent) {
                        HomeBottomBarEvent.OnHomeSelected -> SecurityCenterHomeNavDestination.Home
                        HomeBottomBarEvent.OnNewItemSelected -> SecurityCenterHomeNavDestination.NewItem
                        HomeBottomBarEvent.OnProfileSelected -> SecurityCenterHomeNavDestination.Profile
                        HomeBottomBarEvent.OnSecurityCenterSelected -> null
                    }.also { destination ->
                        onUiEvent(SecurityCenterHomeUiEvent.OnHomeBarNavigation(destination))
                    }
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .padding(all = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            SecurityCenterHomeDarkWebMonitoringSection(
                darkWebMonitoring = darkWebMonitoring,
                onUiEvent = onUiEvent
            )

            if (isSentinelPaidFeature) {
                SecurityCenterRow(
                    title = stringResource(id = R.string.security_center_home_row_sentinel_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_sentinel_subtitle),
                    accentBackgroundColor = PassTheme.colors.interactionNormMinor2,
                    isClickable = true,
                    trailingContent = { PassPlusIcon() },
                    onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowSentinelBottomSheet) }
                )
            } else {
                SecurityCenterToggleRow(
                    title = stringResource(id = R.string.security_center_home_row_sentinel_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_sentinel_subtitle),
                    isChecked = isSentinelEnabled,
                    onClick = {
                        if (isSentinelEnabled) {
                            SecurityCenterHomeUiEvent.OnDisableSentinel
                        } else {
                            SecurityCenterHomeUiEvent.OnShowSentinelBottomSheet
                        }.also(onUiEvent)
                    }
                )
            }

            SectionTitle(text = stringResource(id = R.string.security_center_home_section_password_health))

            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Indicator(
                    title = stringResource(id = R.string.security_center_home_row_insecure_passwords_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_insecure_passwords_subtitle),
                    count = insecurePasswordsCount
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowWeakPasswords) }
            )

            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Indicator(
                    title = stringResource(id = R.string.security_center_home_row_reused_passwords_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_reused_passwords_subtitle),
                    count = reusedPasswordsCount
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowReusedPasswords) }
            )

            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Standard(
                    title = stringResource(id = R.string.security_center_home_row_missing_tfa_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_missing_tfa_subtitle),
                    count = missing2faCount,
                    showPassPlusIcon = false
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowMissingSecondAuthFactors) }
            )

            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Standard(
                    title = stringResource(id = R.string.security_center_home_row_excludes_items_title),
                    subtitle = stringResource(id = R.string.security_center_home_row_excludes_items_subtitle),
                    count = excludedItemsCount,
                    showPassPlusIcon = isExcludedItemsPaidFeature
                ),
                onClick = { onUiEvent(SecurityCenterHomeUiEvent.OnShowExcludedItems) }
            )
        }
    }
}
