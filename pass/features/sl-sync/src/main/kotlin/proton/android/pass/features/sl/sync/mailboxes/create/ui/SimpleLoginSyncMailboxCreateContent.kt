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

package proton.android.pass.features.sl.sync.mailboxes.create.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.create.presentation.SimpleLoginSyncMailboxCreateState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncMailboxCreateContent(
    modifier: Modifier = Modifier,
    state: SimpleLoginSyncMailboxCreateState,
    onUiEvent: (SimpleLoginSyncMailboxCreateUiEvent) -> Unit
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                modifier = modifier,
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(id = R.string.simple_login_sync_mailbox_create_title),
                onUpClick = { onUiEvent(SimpleLoginSyncMailboxCreateUiEvent.OnBackClicked) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.small),
                        isLoading = isLoading,
                        buttonEnabled = canCreateMailbox,
                        color = PassTheme.colors.interactionNormMajor1,
                        text = {
                            Text(
                                text = stringResource(id = CompR.string.action_continue),
                                fontWeight = FontWeight.W400,
                                fontSize = 14.sp,
                                color = PassTheme.colors.textInvert,
                                style = ProtonTheme.typography.defaultSmallNorm,
                                maxLines = 1
                            )
                        },
                        onClick = { onUiEvent(SimpleLoginSyncMailboxCreateUiEvent.OnCreateClicked) }
                    )
                }
            )
        }
    ) { innerPaddingValue ->

    }
}
