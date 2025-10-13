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

package proton.android.pass.features.report.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.report.R
import proton.android.pass.features.report.navigation.ReportNavContentEvent
import proton.android.pass.passkeys.api.PasskeySupport
import proton.android.pass.passkeys.api.PasskeySupport.NotSupportedReason.AndroidVersion
import proton.android.pass.passkeys.api.PasskeySupport.NotSupportedReason.CredentialManagerUnsupported

@Composable
@Suppress("CyclomaticComplexMethod")
internal fun ReportTipsPage(
    modifier: Modifier = Modifier,
    passkeySupportOption: Option<PasskeySupport>,
    reportReasonOption: Option<ReportReason>,
    onEvent: (ReportNavContentEvent) -> Unit,
    onReportIssue: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        val reportReason = reportReasonOption.value() ?: return

        val reportReasonTitle = when (reportReason) {
            ReportReason.Autofill -> stringResource(R.string.report_reason_autofill)
            ReportReason.Sharing -> stringResource(R.string.report_reason_sharing)
            ReportReason.Sync -> stringResource(R.string.report_reason_sync)
            ReportReason.Passkeys -> stringResource(R.string.report_reason_passkeys)
            ReportReason.Other -> stringResource(R.string.report_reason_other)
        }
        Text.Headline(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = stringResource(R.string.tips_title, reportReasonTitle)
        )
        Spacer(Modifier.height(Spacing.small))
        Text.Body1Regular(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = stringResource(R.string.tips_subtitle)
        )
        Spacer(Modifier.height(Spacing.medium))

        val context = LocalContext.current
        when (reportReason) {
            ReportReason.Autofill -> {
                TipRow(
                    text = stringResource(R.string.autofill_tip_restart),
                    onClick = { onEvent(ReportNavContentEvent.OpenAutofillSettings) }
                )
                TipRow(
                    text = stringResource(R.string.autofill_tip_browser)
                )
                TipRow(
                    text = stringResource(R.string.autofill_tip_another_website),
                    onClick = { onEvent(ReportNavContentEvent.OpenTestPage) }
                )
            }

            ReportReason.Sharing -> {
                TipRow(
                    text = stringResource(R.string.sharing_tip_vault_sharing_guide),
                    onClick = { openWebsite(context, PASS_VAULT_SHARE) }
                )
                TipRow(
                    text = stringResource(R.string.sharing_tip_secure_links_guide),
                    onClick = { openWebsite(context, PASS_SECURE_LINK) }
                )
            }

            ReportReason.Sync -> {
                TipRow(text = stringResource(R.string.sync_tip_force_refresh))
            }

            ReportReason.Passkeys -> {
                when (passkeySupportOption) {
                    None -> {}
                    is Some -> {
                        val passKeyLabelText =
                            if (passkeySupportOption.value is PasskeySupport.Supported) {
                                stringResource(R.string.tips_passkey_supported)
                            } else {
                                stringResource(R.string.tips_passkey_not_supported)
                            }
                        Text.Body1Regular(
                            modifier = Modifier
                                .padding(Spacing.medium)
                                .roundedContainer(
                                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                                    borderColor = Color.Transparent
                                )
                                .padding(Spacing.small),
                            text = passKeyLabelText
                        )
                        when (val passkeySupport = passkeySupportOption.value) {
                            is PasskeySupport.NotSupported -> {
                                when (passkeySupport.reason) {
                                    AndroidVersion ->
                                        TipRow(text = stringResource(R.string.tips_passkey_android_not_supported))

                                    CredentialManagerUnsupported ->
                                        TipRow(
                                            text = stringResource(
                                                R.string.tips_passkeys_credential_manager_not_supported
                                            )
                                        )

                                    PasskeySupport.NotSupportedReason.Quest -> {}
                                    PasskeySupport.NotSupportedReason.Unknown -> {}
                                }
                            }

                            PasskeySupport.Supported -> {
                                TipRow(
                                    text = stringResource(R.string.tips_passkey_check_guide),
                                    onClick = { openWebsite(context, PASS_PASSKEYS) }
                                )
                            }
                        }
                    }
                }

            }

            ReportReason.Other ->
                throw IllegalStateException("$reportReason should not be shown here")
        }
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text.Body1Regular(
                stringResource(R.string.hint_tips_screen),
                color = PassTheme.colors.textWeak
            )
        }
        Spacer(Modifier.height(Spacing.small))
        Button.Circular(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .fillMaxWidth(),
            color = PassTheme.colors.interactionNormMajor1,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onReportIssue
        ) {
            Text.Body1Regular(stringResource(R.string.contact_us_button))
        }
        Spacer(Modifier.height(Spacing.small))
        Button.Circular(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .fillMaxWidth(),
            color = Color.Transparent,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onEvent(ReportNavContentEvent.Close) }
        ) {
            Text.Body1Regular(stringResource(R.string.cancel_tips_button))
        }
    }
}

private const val PASS_PASSKEYS = "https://proton.me/support/pass-use-passkeys"
private const val PASS_VAULT_SHARE = "https://proton.me/support/pass-android-share"
private const val PASS_SECURE_LINK = "https://proton.me/support/secure-link-sharing-android"

@Preview
@Composable
fun ReportTipsPagePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ReportTipsPage(
                passkeySupportOption = PasskeySupport.NotSupported(AndroidVersion).some(),
                reportReasonOption = ReportReason.Passkeys.some(),
                onEvent = {},
                onReportIssue = {}
            )
        }
    }
}
