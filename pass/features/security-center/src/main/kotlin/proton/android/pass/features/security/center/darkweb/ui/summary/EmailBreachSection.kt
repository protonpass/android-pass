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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Composable
internal fun EmailBreachSection(
    modifier: Modifier = Modifier,
    state: DarkWebEmailBreachState,
    summaryType: DarkWebSummaryType,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        EmailBreachHeader(
            summaryType = summaryType,
            isClickable = state.list().isNotEmpty(),
            onEvent = onEvent,
            state = state
        )

        val listSize = state.list().take(10).size
        when {
            listSize > 0 -> EmailBreachEmptyList(
                listSize = listSize,
                state = state,
                onEvent = onEvent
            )

            state !is DarkWebEmailBreachState.Success -> Text(
                modifier = Modifier
                    .padding(horizontal = Spacing.medium)
                    .applyIf(
                        condition = state is DarkWebEmailBreachState.Loading,
                        ifTrue = { placeholder() }
                    ),
                text = when (summaryType) {
                    DarkWebSummaryType.Proton ->
                        stringResource(R.string.security_center_dark_web_monitor_proton_addresses_error)
                    DarkWebSummaryType.Alias ->
                        stringResource(R.string.security_center_dark_web_monitor_alias_addresses_error)
                },
                style = PassTheme.typography.body3Weak()
                    .copy(color = PassTheme.colors.passwordInteractionNormMajor2)
            )

            else -> EmailBreachEmptyList(
                state = state,
                summaryType = summaryType,
                isClickable = !state.enabledMonitoring(),
                onEvent = onEvent
            )
        }
    }
}

internal class ThemedDarkWebEmailBreachStatePreviewProvider :
    ThemePairPreviewProvider<DarkWebEmailBreachStatePreview>(DarkWebEmailBreachStateProvider())

@Preview
@Composable
internal fun EmailBreachSectionPreview(
    @PreviewParameter(ThemedDarkWebEmailBreachStatePreviewProvider::class)
    input: Pair<Boolean, DarkWebEmailBreachStatePreview>
) {
    PassTheme(isDark = input.first) {
        Surface {
            EmailBreachSection(
                state = input.second.state,
                summaryType = input.second.summaryType,
                onEvent = {}
            )
        }
    }
}
