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

package proton.android.pass.features.security.center.breachdetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Inverted
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.breachdetail.presentation.SecurityCenterBreachDetailState
import proton.android.pass.features.security.center.shared.ui.image.BreachImage

@Composable
internal fun SecurityCenterBreachDetailBSContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterBreachDetailState,
    onUiEvent: (SecurityCenterBreachDetailUiEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = PassTheme.colors.backgroundNorm)
            .bottomSheet(Spacing.medium),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            state.breachEmail?.let { breachEmail ->
                BreachDetailHeader(breachEmail = breachEmail)
                ExposedData(breachEmail = breachEmail)
                Details(breachEmail = breachEmail)
            }
            RecommendedActions()
            Text(
                text = stringResource(R.string.security_center_report_detail_note),
                style = ProtonTheme.typography.body2Regular
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
        CircleButton(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            color = PassTheme.colors.loginInteractionNormMajor1,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onUiEvent(SecurityCenterBreachDetailUiEvent.OnResolveClick) }
        ) {
            Text(
                text = stringResource(R.string.security_center_report_detail_mark_as_resolved),
                style = PassTheme.typography.body3Inverted(),
                color = PassTheme.colors.textInvert
            )
        }
    }
}

@Composable
internal fun RecommendedActions(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.security_center_report_detail_recommended_actions),
            style = ProtonTheme.typography.body1Medium
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text(
                modifier = Modifier
                    .roundedContainer(
                        backgroundColor = Color.Transparent,
                        borderColor = PassTheme.colors.inputBorderNorm
                    )
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                text = stringResource(R.string.security_center_report_detail_change_password)
            )
            Text(
                modifier = Modifier
                    .roundedContainer(
                        backgroundColor = Color.Transparent,
                        borderColor = PassTheme.colors.inputBorderNorm
                    )
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                text = stringResource(R.string.security_center_report_detail_use_aliases)
            )
            Text(
                modifier = Modifier
                    .roundedContainer(
                        backgroundColor = Color.Transparent,
                        borderColor = PassTheme.colors.inputBorderNorm
                    )
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                text = stringResource(R.string.security_center_report_detail_enable_2fa)
            )
        }
    }
}

@Composable
private fun Details(modifier: Modifier = Modifier, breachEmail: BreachEmail) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        if (breachEmail.email.isNotBlank() || !breachEmail.passwordLastChars.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.security_center_report_detail_details),
                style = ProtonTheme.typography.body1Medium
            )
            breachEmail.email.takeIf { it.isNotBlank() }?.let { email ->
                Column {
                    Text(
                        text = stringResource(R.string.security_center_report_detail_email_address),
                        style = PassTheme.typography.body3Norm()
                    )
                    Text(text = email, style = ProtonTheme.typography.body1Regular)
                }
            }
            breachEmail.passwordLastChars?.let { password ->
                Column {
                    Text(
                        text = stringResource(R.string.security_center_report_detail_password),
                        style = PassTheme.typography.body3Norm()
                    )
                    Text(
                        text = password,
                        style = ProtonTheme.typography.body1Regular
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ExposedData(modifier: Modifier = Modifier, breachEmail: BreachEmail) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        breachEmail.exposedData.takeIf { it.isNotEmpty() }?.let { dataList ->
            Text(
                text = stringResource(R.string.security_center_report_detail_your_exposed_information),
                style = ProtonTheme.typography.body1Medium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                dataList.forEach {
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PassTheme.colors.signalDanger)
                            .padding(Spacing.small),
                        text = it,
                        style = PassTheme.typography.body3Norm(),
                        color = PassTheme.colors.textInvert
                    )
                }
            }
        }
    }
}

@Composable
private fun BreachDetailHeader(modifier: Modifier = Modifier, breachEmail: BreachEmail) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        BreachImage()
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text(text = breachEmail.name, style = ProtonTheme.typography.headline)
            DateUtils.formatDate(breachEmail.publishedAt)
                .onSuccess { date ->
                    val bodyTextResource =
                        stringResource(R.string.security_center_report_detail_subtitle)
                    val bodyText = buildAnnotatedString {
                        val textParts = bodyTextResource.split("__DATE__")
                        if (textParts.size == 2) {
                            append(textParts[0])
                            append(
                                AnnotatedString(
                                    date,
                                    SpanStyle(fontWeight = FontWeight.Bold)
                                )
                            )
                            append(textParts[1])
                        } else {
                            append(bodyTextResource)
                        }
                    }

                    Text(
                        text = bodyText,
                        style = ProtonTheme.typography.body1Regular
                    )
                }
        }
    }
}

