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

package proton.android.pass.features.secure.links.create.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.create.presentation.SecureLinksCreateState
import proton.android.pass.features.secure.links.create.ui.rows.SecureLinkCreateExpirationRow
import proton.android.pass.features.secure.links.create.ui.rows.SecureLinkCreateMaxViewsRow

@Composable
internal fun SecureLinksCreateContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit,
    state: SecureLinksCreateState,
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                title = stringResource(id = R.string.secure_links_create_title),
                onUpClick = { onUiEvent(SecureLinksCreateUiEvent.OnBackArrowClicked) }
            )
        },
        bottomBar = {
            PassCircleButton(
                modifier = Modifier.padding(all = Spacing.medium),
                text = stringResource(id = R.string.secure_links_create_button_generate),
                onClick = { onUiEvent(SecureLinksCreateUiEvent.OnGenerateLinkClicked) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = Spacing.medium)
        ) {
            SecureLinkCreateExpirationRow(
                expiration = expiration,
                onUiEvent = onUiEvent
            )

            SecureLinkCreateMaxViewsRow(
                isMaxViewsEnabled = isMaxViewsEnabled,
                isMaxViewsDecreaseEnabled = isMaxViewsDecreaseEnabled,
                maxViewsAllowed = maxViewsAllowed,
                onUiEvent = onUiEvent
            )
        }
    }
}
