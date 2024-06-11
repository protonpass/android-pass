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

package proton.android.pass.features.secure.links.overview.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewState

@Composable
internal fun SecureLinksOverviewContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksOverviewUiEvent) -> Unit,
    state: SecureLinksOverviewState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.Cross,
                onUpClick = { onUiEvent(SecureLinksOverviewUiEvent.OnCloseClicked) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(all = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                PassCircleButton(
                    text = stringResource(id = R.string.secure_links_overview_button_copy_link),
                    onClick = { onUiEvent(SecureLinksOverviewUiEvent.OnCopyLinkClicked) }
                )

                PassCircleButton(
                    text = stringResource(id = R.string.secure_links_overview_button_share_link),
                    textColor = PassTheme.colors.interactionNormMajor2,
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    onClick = { onUiEvent(SecureLinksOverviewUiEvent.OnShareLinkClicked) }
                )
            }
        }
    ) { innerPaddingValue ->

    }
}
