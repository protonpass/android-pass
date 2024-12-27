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

package proton.android.pass.features.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.passkeys.api.PasskeySupport
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PasskeyProfileSection(modifier: Modifier = Modifier, support: ProfilePasskeySupportSection.Show) {
    val status = support.toStatus()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .roundedContainerNorm()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profile_passkey_support_section_title),
                style = ProtonTheme.typography.defaultWeak,
                color = PassTheme.colors.textNorm
            )
            Text(
                text = status.subtitle,
                color = status.color
            )
        }

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(status.icon),
            tint = status.color,
            contentDescription = null
        )
    }
}

@Composable
private fun ProfilePasskeySupportSection.Show.toStatus(): PasskeyStatus = when (support) {
    is PasskeySupport.NotSupported -> PasskeyStatus(
        icon = CompR.drawable.ic_shield_danger,
        color = PassTheme.colors.signalDanger,
        subtitle = when (support.reason) {
            PasskeySupport.NotSupportedReason.AndroidVersion -> {
                stringResource(R.string.profile_passkey_support_subtitle_not_supported_android_version)
            }

            PasskeySupport.NotSupportedReason.CredentialManagerUnsupported -> {
                stringResource(R.string.profile_passkey_support_subtitle_not_supported_credential_manager)
            }

            PasskeySupport.NotSupportedReason.Unknown -> {
                stringResource(R.string.profile_passkey_support_subtitle_not_supported_unknown)
            }
        }
    )

    PasskeySupport.Supported -> PasskeyStatus(
        icon = CompR.drawable.ic_shield_success,
        color = PassTheme.colors.signalSuccess,
        subtitle = stringResource(R.string.profile_passkey_support_subtitle_supported)
    )
}

private data class PasskeyStatus(
    @DrawableRes val icon: Int,
    val color: Color,
    val subtitle: String
)

@Preview
@Composable
internal fun PasskeyProfileSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val support = if (input.second) {
        ProfilePasskeySupportSection.Show(PasskeySupport.Supported)
    } else {
        ProfilePasskeySupportSection.Show(
            support = PasskeySupport.NotSupported(
                reason = PasskeySupport.NotSupportedReason.AndroidVersion
            )
        )
    }
    PassTheme(isDark = input.first) {
        Surface {
            PasskeyProfileSection(
                support = support
            )
        }
    }
}
