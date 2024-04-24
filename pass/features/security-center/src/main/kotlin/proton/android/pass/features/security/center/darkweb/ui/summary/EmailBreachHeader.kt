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

package proton.android.pass.features.security.center.darkweb.ui.summary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Composable
internal fun EmailBreachHeader(
    modifier: Modifier = Modifier,
    summaryType: DarkWebSummaryType,
    state: DarkWebEmailBreachState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    val list = state.list()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    when (summaryType) {
                        DarkWebSummaryType.Proton -> onEvent(DarkWebUiEvent.OnShowAllProtonEmailBreachClick)
                        DarkWebSummaryType.Alias -> onEvent(DarkWebUiEvent.OnShowAllAliasEmailBreachClick)
                    }
                }
            )
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        val accentColor = when {
            state.enabledMonitoring() && list.isEmpty() -> PassTheme.colors.cardInteractionNormMajor1
            state is DarkWebEmailBreachState.Error -> PassTheme.colors.passwordInteractionNormMajor1
            !state.enabledMonitoring() -> PassTheme.colors.textWeak
            else -> PassTheme.colors.passwordInteractionNormMajor1
        }
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            val (title, subtitle) = when (summaryType) {
                DarkWebSummaryType.Proton -> {
                    val title =
                        stringResource(R.string.security_center_dark_web_monitor_proton_addresses)
                    val subtitle = when {
                        state is DarkWebEmailBreachState.Error ->
                            stringResource(R.string.security_center_dark_web_monitor_proton_addresses_error)

                        !state.enabledMonitoring() ->
                            stringResource(R.string.security_center_dark_web_monitor_monitoring_disabled)

                        else -> pluralStringResource(
                            R.plurals.security_center_dark_web_monitor_found_in_breaches,
                            list.count(),
                            list.count()
                        )
                    }
                    title to subtitle
                }

                DarkWebSummaryType.Alias -> {
                    val title =
                        stringResource(R.string.security_center_dark_web_monitor_hide_my_email_aliases)
                    val subtitle = when {
                        state is DarkWebEmailBreachState.Error ->
                            stringResource(R.string.security_center_dark_web_monitor_alias_addresses_error)

                        !state.enabledMonitoring() ->
                            stringResource(R.string.security_center_dark_web_monitor_monitoring_disabled)

                        else -> pluralStringResource(
                            R.plurals.security_center_dark_web_monitor_found_in_breaches,
                            list.count(),
                            list.count()
                        )
                    }
                    title to subtitle
                }
            }
            Text(
                text = title,
                style = ProtonTheme.typography.defaultNorm
            )
            Text(
                modifier = Modifier.applyIf(
                    condition = state is DarkWebEmailBreachState.Loading,
                    ifTrue = { placeholder() }
                ),
                text = subtitle,
                style = PassTheme.typography.body3Weak().copy(color = accentColor)
            )
        }

        Icon(
            painter = painterResource(proton.android.pass.composecomponents.impl.R.drawable.ic_chevron_tiny_right),
            contentDescription = null,
            tint = accentColor.takeIf { list.isNotEmpty() } ?: LocalContentColor.current.copy(
                alpha = LocalContentAlpha.current
            )
        )
    }
    if (list.isNotEmpty()) {
        PassDivider(modifier = Modifier.padding(horizontal = Spacing.small))
    }
}
