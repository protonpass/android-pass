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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.report.navigation.ReportNavContentEvent

@Composable
internal fun ReportTipsPage(
    modifier: Modifier = Modifier,
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
            ReportReason.Autofill -> "Autofill"
            ReportReason.Sharing -> "Sharing"
            ReportReason.Sync -> "Sync"
            ReportReason.Passkeys -> "Passkeys"
            ReportReason.Other -> "Other"
        }
        Text.Headline(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = "Quick fixes for $reportReasonTitle"
        )
        Spacer(Modifier.height(Spacing.small))
        Text.Body1Regular(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = "These tips could help you solve your issue faster."
        )
        Spacer(Modifier.height(Spacing.medium))

        val context = LocalContext.current
        when (reportReason) {
            ReportReason.Autofill -> {
                TipRow(
                    text = "Try disabling and enabling the autofill service. If that doesn't work try another browser.",
                    onClick = { onEvent(ReportNavContentEvent.OpenAutofillSettings) }
                )
            }

            ReportReason.Sharing -> {
                TipRow(
                    text = "Having issues with vault sharing? Check our guide.",
                    onClick = { openWebsite(context, PASS_VAULT_SHARE) }
                )
                TipRow(
                    text = "Having issues with secure links? Check our guide.",
                    onClick = { openWebsite(context, PASS_SECURE_LINK) }
                )
            }

            ReportReason.Sync -> {
                TipRow(text = "Is your screen empty? try doing a force sync.")
            }

            ReportReason.Passkeys -> {
                TipRow(text = "Check your browser settings")
            }

            ReportReason.Other ->
                throw IllegalStateException("$reportReason should not be shown here")
        }
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text.Body1Regular("Didn't work?", color = PassTheme.colors.textWeak)
        }
        Spacer(Modifier.height(Spacing.small))
        Button.Circular(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .fillMaxWidth(),
            color = PassTheme.colors.loginInteractionNormMajor1,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onReportIssue
        ) {
            Text.Body1Regular("Contact us")
        }
        Spacer(Modifier.height(Spacing.small))
        Button.Circular(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .fillMaxWidth(),
            color = Color.Transparent,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onEvent(ReportNavContentEvent.CancelTips) }
        ) {
            Text.Body1Regular("Cancel")
        }
    }
}

private const val PASS_VAULT_SHARE = "https://proton.me/support/pass-android-share"
private const val PASS_SECURE_LINK = "https://proton.me/support/secure-link-sharing-android"


@Preview
@Composable
fun ReportTipsPagePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ReportTipsPage(
                reportReasonOption = ReportReason.Autofill.some(),
                onEvent = {},
                onReportIssue = {}
            )
        }
    }
}
