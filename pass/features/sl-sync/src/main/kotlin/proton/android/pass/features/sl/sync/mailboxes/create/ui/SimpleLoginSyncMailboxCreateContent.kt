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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.create.presentation.SimpleLoginSyncMailboxCreateState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncMailboxCreateContent(
    modifier: Modifier = Modifier,
    mailboxEmail: String,
    state: SimpleLoginSyncMailboxCreateState,
    onUiEvent: (SimpleLoginSyncMailboxCreateUiEvent) -> Unit
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(id = R.string.simple_login_sync_mailbox_create_title),
                onUpClick = { onUiEvent(SimpleLoginSyncMailboxCreateUiEvent.OnBackClicked) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = isLoading,
                        buttonEnabled = canCreateMailbox(mailboxEmail),
                        color = if (canCreateMailbox(mailboxEmail)) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor1
                        },
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
        val focusRequester = remember { FocusRequester() }

        ProtonTextField(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(
                    horizontal = Spacing.medium,
                    vertical = Spacing.large
                )
                .focusRequester(focusRequester = focusRequester),
            value = mailboxEmail,
            textStyle = ProtonTheme.typography.subheadlineNorm,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            editable = canEditMailboxEmail,
            isError = showInvalidMailboxEmailError,
            errorMessage = stringResource(id = CompR.string.email_address_invalid),
            placeholder = {
                ProtonTextFieldPlaceHolder(
                    text = stringResource(CompR.string.email_address),
                    textStyle = ProtonTheme.typography.subheadlineNorm
                        .copy(color = ProtonTheme.colors.textHint)
                )
            },
            moveToNextOnEnter = true,
            onChange = { newMailboxEmail ->
                SimpleLoginSyncMailboxCreateUiEvent.OnMailboxEmailChanged(
                    newMailboxEmail = newMailboxEmail
                ).also(onUiEvent)
            }
        )

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }
}
