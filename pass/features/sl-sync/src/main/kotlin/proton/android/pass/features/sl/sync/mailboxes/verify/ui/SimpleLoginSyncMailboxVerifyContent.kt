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

package proton.android.pass.features.sl.sync.mailboxes.verify.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.screens.PassCodeVerificationScreen
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.verify.presentation.SimpleLoginSyncMailboxVerifyState

@Composable
internal fun SimpleLoginSyncMailboxVerifyContent(
    modifier: Modifier = Modifier,
    verificationCode: String,
    state: SimpleLoginSyncMailboxVerifyState,
    onUiEvent: (SimpleLoginSyncMailboxVerifyUiEvent) -> Unit
) = with(state) {
    PassCodeVerificationScreen(
        modifier = modifier,
        topBarTitle = stringResource(id = R.string.simple_login_sync_mailbox_verify_title),
        topBarSubtitle = stringResource(
            id = R.string.simple_login_sync_mailbox_verify_subtitle,
            mailboxEmail.ifEmpty {
                stringResource(id = R.string.simple_login_sync_mailbox_verify_email_fallback)
            }
        ),
        onUpClick = { onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnCloseClicked) },
        isActionLoading = isLoading,
        onActionClick = {
            onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnVerifyClicked)
        },
        verificationCode = verificationCode,
        verificationCodeLength = verificationCodeLength,
        canEnterVerificationCode = canEnterVerificationCode,
        onVerificationCodeChange = { newVerificationCode ->
            SimpleLoginSyncMailboxVerifyUiEvent.OnVerificationCodeChanged(
                newVerificationCode = newVerificationCode
            ).also(onUiEvent)
        },
        onResendVerificationCodeClick = {
            onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnResendVerificationCodeClicked)
        }
    )

//    Scaffold(
//        modifier = modifier,
//        topBar = {
//            PassExtendedTopBar(
//                backButton = PassTopBarBackButtonType.Cross,
//                title = stringResource(id = R.string.simple_login_sync_mailbox_verify_title),
//                subtitle = stringResource(
//                    id = R.string.simple_login_sync_mailbox_verify_subtitle,
//                    mailboxEmail.ifEmpty {
//                        stringResource(id = R.string.simple_login_sync_mailbox_verify_email_fallback)
//                    }
//                ),
//                onUpClick = { onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnCloseClicked) },
//                actions = {
//                    LoadingCircleButton(
//                        modifier = Modifier.padding(vertical = Spacing.small),
//                        isLoading = isLoading,
//                        buttonEnabled = canVerifyMailbox(verificationCode),
//                        color = if (canVerifyMailbox(verificationCode)) {
//                            PassTheme.colors.interactionNormMajor1
//                        } else {
//                            PassTheme.colors.interactionNormMajor1.copy(alpha = 0.6f)
//                        },
//                        text = {
//                            Text(
//                                text = stringResource(id = CompR.string.action_continue),
//                                fontWeight = FontWeight.W400,
//                                fontSize = 14.sp,
//                                color = PassTheme.colors.textInvert,
//                                style = ProtonTheme.typography.defaultSmallNorm,
//                                maxLines = 1
//                            )
//                        },
//                        onClick = {
//                            onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnVerifyClicked)
//                        }
//                    )
//                }
//            )
//        }
//    ) { innerPaddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues = innerPaddingValues)
//                .padding(
//                    horizontal = Spacing.medium,
//                    vertical = Spacing.large
//                ),
//            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
//        ) {
//            SimpleLoginSyncMailboxVerifyCodeSection(
//                verificationCode = verificationCode,
//                verificationCodeLength = verificationCodeLength,
//                verificationCodeTimerSeconds = verificationCodeTimerSeconds,
//                showResendVerificationCodeTimer = showResendVerificationCodeTimer,
//                canRequestVerificationCode = canRequestVerificationCode,
//                canEnterVerificationCode = canEnterVerificationCode,
//                onVerificationCodeChange = { newVerificationCode ->
//                    SimpleLoginSyncMailboxVerifyUiEvent.OnVerificationCodeChanged(
//                        newVerificationCode = newVerificationCode
//                    ).also(onUiEvent)
//                },
//                onResendVerificationCodeClick = {
//                    onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnResendVerificationCodeClicked)
//                }
//            )
//        }
//    }
}
