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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Composable
internal fun EmailBreachHeader(
    modifier: Modifier = Modifier,
    summaryType: DarkWebSummaryType,
    state: DarkWebEmailBreachState,
    isClickable: Boolean,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            val title = when (summaryType) {
                DarkWebSummaryType.Proton ->
                    stringResource(R.string.security_center_dark_web_monitor_proton_addresses)
                DarkWebSummaryType.Alias ->
                    stringResource(R.string.security_center_dark_web_monitor_hide_my_email_aliases)
            }
            val titleWithCount = if (state.list().isNotEmpty()) {
                title.plus(" (${state.list().size})")
            } else {
                title
            }
            Text(
                modifier = Modifier.padding(vertical = Spacing.medium),
                text = titleWithCount,
                style = ProtonTheme.typography.defaultSmallNorm
            )
        }

        if (isClickable) {
            TransparentTextButton(
                text = stringResource(R.string.security_center_dark_web_monitor_see_all),
                color = PassTheme.colors.interactionNormMajor2,
                onClick = {
                    when (summaryType) {
                        DarkWebSummaryType.Proton -> onEvent(DarkWebUiEvent.OnShowAllProtonEmailBreachClick)
                        DarkWebSummaryType.Alias -> onEvent(DarkWebUiEvent.OnShowAllAliasEmailBreachClick)
                    }
                }
            )
        }
    }
}
