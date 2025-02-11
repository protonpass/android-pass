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

package proton.android.pass.features.sl.sync.mailboxes.options.ui

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsAction
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsEvent
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsState

@Composable
internal fun SimpleLoginSyncMailboxOptionsContent(
    modifier: Modifier = Modifier,
    state: SimpleLoginSyncMailboxOptionsState,
    onUiEvent: (SimpleLoginSyncMailboxOptionsUiEvent) -> Unit
) = with(state) {
    buildList {
        if (canChangeMailbox) {
            changeEmail(
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnChangeEmailClicked)
                }
            ).also(::add)
        }

        if (canSetAsDefault) {
            setAsDefault(
                action = action,
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnSetAsDefaultClicked)
                }
            ).also(::add)
        }

        if (canVerify) {
            verify(
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnVerifyClicked)
                }
            ).also(::add)
        }

        if (canCancelMailboxChange) {
            cancelChangeEmail(
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnCancelChangeEmailClicked)
                }
            ).also(::add)
        }

        if (canDelete) {
            delete(
                action = action,
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnDeleteClicked)
                }
            ).also(::add)
        }
    }.let { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.toPersistentList()
        )
    }
}

@Preview
@Composable
fun SLSyncMailboxOptionsContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncMailboxOptionsContent(
                state = SimpleLoginSyncMailboxOptionsState(
                    action = SimpleLoginSyncMailboxOptionsAction.None,
                    aliasMailboxOption = SimpleLoginAliasMailbox(
                        id = 0,
                        email = "myemail@proton.me",
                        pendingEmail = null,
                        isDefault = false,
                        isVerified = true,
                        aliasCount = 0
                    ).some(),
                    event = SimpleLoginSyncMailboxOptionsEvent.Idle
                ),
                onUiEvent = {}
            )
        }
    }
}
