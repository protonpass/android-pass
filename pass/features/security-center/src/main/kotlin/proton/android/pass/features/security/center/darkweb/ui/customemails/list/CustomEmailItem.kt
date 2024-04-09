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

package proton.android.pass.features.security.center.darkweb.ui.customemails.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiState
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiStatus

@Composable
internal fun CustomEmailItem(
    modifier: Modifier = Modifier,
    email: CustomEmailUiState,
    onAddClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(PassTheme.shapes.squircleMediumShape)
                .background(PassTheme.colors.interactionNormMinor1),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = me.proton.core.presentation.R.drawable.ic_proton_envelope),
                contentDescription = null,
                tint = PassTheme.colors.interactionNormMajor2
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = email.email,
                style = ProtonTheme.typography.body1Regular
            )

            when (email.status) {
                is CustomEmailUiStatus.NotVerified -> {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.security_center_dark_web_monitor_custom_emails_used_count,
                            count = email.status.usedInLoginsCount,
                            email.status.usedInLoginsCount
                        ),
                        style = PassTheme.typography.body3Weak()
                    )
                }
                is CustomEmailUiStatus.Verified -> {
                    val color = if (email.status.breachesDetected > 0) {
                        PassTheme.colors.noteInteractionNormMajor1
                    } else {
                        PassTheme.colors.cardInteractionNormMajor1
                    }
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.security_center_dark_web_monitor_custom_emails_breaches_found,
                            count = email.status.breachesDetected,
                            email.status.breachesDetected
                        ),
                        style = PassTheme.typography.body3Weak(),
                        color = color
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(PassTheme.colors.interactionNormMinor1)
                .clickable(onClick = onAddClick)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                text = stringResource(id = R.string.security_center_dark_web_monitor_custom_emails_add_button),
                style = ProtonTheme.typography.captionMedium,
                color = PassTheme.colors.interactionNormMajor2
            )
        }
    }
}

@Preview
@Composable
fun CustomEmailItemPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val status = if (input.second) {
        CustomEmailUiStatus.NotVerified(usedInLoginsCount = 3)
    } else {
        CustomEmailUiStatus.Verified(breachesDetected = 2)
    }
    PassTheme(isDark = input.first) {
        Surface {
            CustomEmailItem(
                email = CustomEmailUiState(
                    id = BreachCustomEmailId("1"),
                    email = "some@test.email",
                    status = status
                ),
                onAddClick = {}
            )
        }
    }
}
