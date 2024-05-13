/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.security.center.verifyemail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.ChevronDownIcon
import proton.android.pass.features.security.center.R

private const val LINK_ANNOTATION_TAG = "link"
private const val ROTATION_INITIAL = 0f
private const val ROTATION_TOGGLED = 180f

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun CodeNotReceived(modifier: Modifier = Modifier, onUiEvent: (SecurityCenterVerifyEmailUiEvent) -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        var showRequestCode by remember { mutableStateOf(false) }
        var rotation by remember { mutableFloatStateOf(ROTATION_INITIAL) }
        val displayRotation by animateFloatAsState(
            targetValue = rotation,
            label = "displayRotation"
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.small))
                .padding(vertical = Spacing.small)
                .clickable {
                    showRequestCode = !showRequestCode
                    rotation =
                        if (rotation == ROTATION_INITIAL) ROTATION_TOGGLED else ROTATION_INITIAL
                },
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.security_center_verify_email_code_resend_header),
                style = ProtonTheme.typography.defaultWeak
            )

            ChevronDownIcon(
                modifier = Modifier
                    .size(16.dp)
                    .rotate(displayRotation),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
        AnimatedVisibility(visible = showRequestCode) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    text = stringResource(R.string.security_center_verify_email_code_resend_body),
                    style = ProtonTheme.typography.defaultWeak
                )
                val linkText =
                    stringResource(R.string.security_center_verify_email_code_resend_link_link)
                val text = buildAnnotatedString {
                    withStyle(style = ProtonTheme.typography.defaultWeak.toSpanStyle()) {
                        append(
                            stringResource(
                                id = R.string.security_center_verify_email_code_resend_link_body
                            )
                        )
                    }
                    append(" ")
                    withStyle(
                        style = ProtonTheme.typography.defaultWeak
                            .copy(color = PassTheme.colors.interactionNormMajor2)
                            .toSpanStyle()
                    ) {
                        withAnnotation(
                            tag = LINK_ANNOTATION_TAG,
                            annotation = ""
                        ) {
                            append(linkText)
                        }
                    }
                }

                ClickableText(
                    text = text,
                    onClick = {
                        text.getStringAnnotations(LINK_ANNOTATION_TAG, it, it).firstOrNull()
                            ?.let { _ ->
                                onUiEvent(SecurityCenterVerifyEmailUiEvent.ResendCode)
                            }
                    }
                )
            }
        }
    }
}
