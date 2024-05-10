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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiState
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiStatus
import me.proton.core.presentation.R as CoreR

@Composable
internal fun CustomEmailItem(
    modifier: Modifier = Modifier,
    email: CustomEmailUiState,
    onAddClick: () -> Unit,
    onOptionsClick: (CustomEmailId) -> Unit,
    onDetailClick: (CustomEmailId) -> Unit
) {
    when (email.status) {
        is CustomEmailUiStatus.Suggestion -> {
            CustomEmailSuggestion(
                modifier = modifier,
                email = email.email,
                status = email.status,
                loading = email.status.isLoadingState,
                onAddClick = onAddClick
            )
        }

        is CustomEmailUiStatus.Verified -> {
            CustomEmailItemVerified(
                modifier = modifier,
                email = email.email,
                status = email.status,
                onDetailClick = { onDetailClick(email.status.id) }
            )
        }

        is CustomEmailUiStatus.Unverified -> {
            CustomEmailItemUnverified(
                modifier = modifier,
                email = email.email,
                onOptionsClick = { onOptionsClick(email.status.id) }
            )
        }
    }

}

@Composable
private fun CustomEmailSuggestion(
    modifier: Modifier = Modifier,
    email: String,
    status: CustomEmailUiStatus.Suggestion,
    loading: IsLoadingState,
    onAddClick: () -> Unit
) {
    CustomEmailRow(
        modifier = modifier,
        title = email,
        subtitle = {
            Text(
                text = pluralStringResource(
                    id = R.plurals.security_center_dark_web_monitor_custom_emails_used_count,
                    count = status.usedInLoginsCount,
                    status.usedInLoginsCount
                ),
                style = PassTheme.typography.body3Weak()
            )
        },
        trailingContent = {
            LoadingCircleButton(
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.security_center_dark_web_monitor_custom_emails_add_button
                        ),
                        style = ProtonTheme.typography.captionMedium,
                        color = PassTheme.colors.interactionNormMajor2
                    )
                },
                color = PassTheme.colors.interactionNormMinor1,
                loadingColor = PassTheme.colors.interactionNormMajor2,
                isLoading = loading.value(),
                onClick = onAddClick
            )
        }
    )
}


@Composable
private fun CustomEmailItemUnverified(
    modifier: Modifier = Modifier,
    email: String,
    onOptionsClick: () -> Unit
) {
    CustomEmailRow(
        modifier = modifier,
        title = email,
        subtitle = {
            Text(
                text = stringResource(R.string.security_center_dark_web_monitor_custom_emails_not_verified_subtitle),
                style = PassTheme.typography.body3Weak()
            )
        },
        trailingContent = {
            ThreeDotsMenuButton(onClick = onOptionsClick)
        }
    )
}

@Composable
private fun CustomEmailRow(
    modifier: Modifier,
    title: String,
    subtitle: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = ProtonTheme.typography.body1Regular
            )

            subtitle()
        }

        trailingContent()
    }
}

@Composable
private fun CustomEmailItemVerified(
    modifier: Modifier = Modifier,
    email: String,
    status: CustomEmailUiStatus.Verified,
    onDetailClick: () -> Unit
) {
    val color = if (status.hasBreaches) {
        PassTheme.colors.passwordInteractionNormMajor2
    } else {
        PassTheme.colors.cardInteractionNormMajor2
    }

    Row(
        modifier = modifier
            .clickable(onClick = onDetailClick)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text(
                text = email,
                style = ProtonTheme.typography.body1Regular
            )

            if (status.breachesDetected == 0) {
                Text(
                    text = stringResource(R.string.security_center_dark_web_monitor_found_in_breaches),
                    style = PassTheme.typography.body3Weak(),
                    color = color
                )
            } else {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.security_center_dark_web_monitor_custom_emails_breaches_found,
                        count = status.breachesDetected,
                        status.breachesDetected
                    ),
                    style = PassTheme.typography.body3Weak(),
                    color = color
                )
            }
        }

        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_chevron_right),
            contentDescription = null,
            tint = PassTheme.colors.textWeak
        )
    }
}

@Preview
@Composable
internal fun CustomEmailItemPreview(
    @PreviewParameter(ThemeCustomEmailItemPreviewProvider::class) input: Pair<Boolean, CustomEmailUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CustomEmailItem(
                email = input.second,
                onAddClick = {},
                onDetailClick = {},
                onOptionsClick = {}
            )
        }
    }
}
