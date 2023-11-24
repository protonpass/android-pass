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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.data.api.usecases.DefaultBrowser
import me.proton.core.presentation.R as CoreR

@Composable
fun AutofillProfileSection(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    userBrowser: DefaultBrowser,
    onClick: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingToggle(
            modifier = Modifier.roundedContainerNorm(),
            text = stringResource(R.string.profile_option_autofill),
            isChecked = isChecked,
            belowContent = {
                AnimatedVisibility(visible = isChecked && userBrowser == DefaultBrowser.Samsung) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(CoreR.drawable.ic_proton_exclamation_triangle_filled),
                            contentDescription = null,
                            tint = ProtonTheme.colors.notificationWarning
                        )

                        Text(
                            text = stringResource(R.string.profile_option_autofill_samsung_not_supported),
                            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
                        )
                    }
                }
            },
            onClick = { onClick(isChecked) }
        )
        Text(
            text = stringResource(R.string.profile_option_autofill_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
    }
}

@Preview
@Composable
fun AutofillProfileSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val browser = if (input.second) DefaultBrowser.Other else DefaultBrowser.Samsung
    PassTheme(isDark = input.first) {
        Surface {
            AutofillProfileSection(
                isChecked = true,
                userBrowser = browser,
                onClick = {}
            )
        }
    }
}
