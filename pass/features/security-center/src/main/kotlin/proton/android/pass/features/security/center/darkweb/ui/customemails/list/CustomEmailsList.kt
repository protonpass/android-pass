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

package proton.android.pass.features.security.center.darkweb.ui.customemails.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebCustomEmailsState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsError
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebUiState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Suppress("LongMethod")
@Composable
internal fun CustomEmailsList(
    modifier: Modifier = Modifier,
    state: DarkWebUiState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    when (state.customEmailState) {
        is DarkWebCustomEmailsState.Error ->
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium),
                text = when (state.customEmailState.reason) {
                    DarkWebEmailsError.CannotLoad -> stringResource(
                        R.string.security_center_dark_web_monitor_custom_emails_loading_error
                    )

                    DarkWebEmailsError.Unknown -> stringResource(
                        R.string.security_center_dark_web_monitor_custom_emails_unknown_error
                    )
                },
                style = ProtonTheme.typography.defaultNorm
                    .copy(color = ProtonTheme.colors.notificationError)
            )

        DarkWebCustomEmailsState.Loading ->
            CircularProgressIndicator(modifier.size(48.dp))

        is DarkWebCustomEmailsState.Success -> Column(
            modifier
                .padding(horizontal = Spacing.medium)
                .roundedContainerNorm()
                .padding(vertical = Spacing.small)
        ) {
            val totalCustomEmails = state.customEmailState.emails.size
            val totalSuggestions = state.customEmailState.suggestions.size

            repeat(state.customEmailState.emails.size) {
                val item = state.customEmailState.emails[it]
                CustomEmailItem(
                    email = item,
                    onAddClick = {},
                    onDetailClick = { id ->
                        onEvent(
                            DarkWebUiEvent.OnCustomEmailReportClick(
                                id = id,
                                email = item.email
                            )
                        )
                    },
                    onOptionsClick = { customEmailId ->
                        val event = DarkWebUiEvent.OnUnverifiedEmailOptionsClick(
                            id = customEmailId,
                            email = item.email
                        )
                        onEvent(event)
                    }
                )

                if (it < totalCustomEmails - 1 || totalSuggestions > 0) {
                    PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
                }
            }

            repeat(state.customEmailState.suggestions.size) {
                val item = state.customEmailState.suggestions[it]
                CustomEmailItem(
                    email = item,
                    onAddClick = {
                        onEvent(DarkWebUiEvent.OnAddCustomEmailClick(item.email))
                    },
                    onDetailClick = {},
                    onOptionsClick = {}
                )
                if (it < totalSuggestions - 1) {
                    PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
                }
            }
        }
    }
}
