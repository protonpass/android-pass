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

package proton.android.pass.featureprofile.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
fun FingerprintProfileSection(
    modifier: Modifier = Modifier,
    isFingerprintEnabled: Boolean,
    onFingerprintToggle: (Boolean) -> Unit,
    onAppLockClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.profile_option_fingerprint),
                isChecked = isFingerprintEnabled,
                onClick = { onFingerprintToggle(isFingerprintEnabled) }
            )
            AnimatedVisibility(visible = isFingerprintEnabled) {
                Divider(color = PassTheme.colors.inputBorderNorm)
                SettingOption(
                    text = stringResource(R.string.profile_option_app_lock),
                    onClick = onAppLockClick
                )
            }
        }
        Text(
            text = stringResource(R.string.profile_option_fingerprint_subtitle),
            style = ProtonTheme.typography.captionNorm.copy(PassTheme.colors.textWeak)
        )
    }
}

@Preview
@Composable
fun FingerprintProfileSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            FingerprintProfileSection(
                isFingerprintEnabled = input.second,
                onFingerprintToggle = {},
                onAppLockClick = {}
            )
        }
    }
}
