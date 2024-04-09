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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsError
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Composable
internal fun CustomEmailsList(
    modifier: Modifier = Modifier,
    state: DarkWebEmailsState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {

        CustomEmailsHeader(
            onAddClick = { onEvent(DarkWebUiEvent.OnNewCustomEmailClick) }
        )

        when (state) {
            is DarkWebEmailsState.Error -> {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = when (state.reason) {
                        DarkWebEmailsError.CannotLoad -> stringResource(
                            R.string.security_center_dark_web_monitor_custom_emails_loading_error
                        )
                        DarkWebEmailsError.Unknown -> stringResource(
                            R.string.security_center_dark_web_monitor_custom_emails_unknown_error
                        )
                    }
                )
            }
            DarkWebEmailsState.Loading -> {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            }
            is DarkWebEmailsState.Success -> {
                LazyColumn {
                    items(items = state.emails, key = { it.id }) { email ->
                        CustomEmailItem(
                            email = email,
                            onAddClick = { onEvent(DarkWebUiEvent.OnAddCustomEmailClick(email.id)) }
                        )
                    }
                }
            }
        }
    }
}
