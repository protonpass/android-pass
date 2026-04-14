/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.home.localinappmessages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.home.R
import me.proton.core.presentation.R as CoreR

@Composable
fun LocalInAppMessageBanner(
    modifier: Modifier = Modifier,
    message: InAppMessage.Local,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (message) {
        InAppMessage.Local.Autofill -> stringResource(R.string.home_autofill_banner_title)
        InAppMessage.Local.NotificationPermission ->
            stringResource(R.string.home_notification_permission_banner_title)
        is InAppMessage.Local.SLSync -> stringResource(R.string.sl_sync_banner_title)
    }
    val body = when (message) {
        InAppMessage.Local.Autofill -> stringResource(R.string.home_autofill_banner_text)
        InAppMessage.Local.NotificationPermission ->
            stringResource(R.string.home_notification_permission_banner_text)
        is InAppMessage.Local.SLSync ->
            pluralStringResource(R.plurals.sl_sync_banner_text, message.aliasCount, message.aliasCount)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(
                    start = Spacing.medium + Spacing.extraSmall,
                    end = Spacing.medium + Spacing.extraSmall,
                    top = Spacing.medium + Spacing.extraSmall
                )
                .roundedContainer(
                    backgroundColor = PassTheme.colors.backgroundMedium,
                    borderColor = PassTheme.colors.inputBorderNorm
                )
                .clickable { onClick() }
                .padding(Spacing.mediumSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text.CaptionMedium(title)
                Text.CaptionRegular(body)
            }
            Icon.Default(
                id = CoreR.drawable.ic_proton_chevron_right,
                tint = PassTheme.colors.textWeak
            )
        }
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onDismiss
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 2.dp,
                        color = PassTheme.colors.backgroundNorm,
                        shape = CircleShape
                    )
                    .padding(2.dp)
                    .border(
                        width = 1.dp,
                        color = PassTheme.colors.inputBorderNorm,
                        shape = CircleShape
                    )
                    .padding(1.dp)
                    .background(
                        color = PassTheme.colors.backgroundMedium,
                        shape = CircleShape
                    )
                    .padding(Spacing.extraSmall),
                painter = painterResource(CoreR.drawable.ic_proton_cross_small),
                tint = ProtonTheme.colors.iconNorm,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun LocalInAppMessageBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LocalInAppMessageBanner(
                message = InAppMessage.Local.Autofill,
                onClick = {},
                onDismiss = {}
            )
        }
    }
}
