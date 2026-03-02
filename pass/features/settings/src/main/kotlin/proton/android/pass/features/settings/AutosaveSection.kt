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

package proton.android.pass.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
internal fun AutosaveSection(
    modifier: Modifier = Modifier,
    isAutosaveEnabled: Boolean,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.settings_autofill_section_title),
            style = ProtonTheme.typography.defaultSmallWeak
        )

        Column(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.settings_autosave_prompt_title),
                isChecked = isAutosaveEnabled,
                onClick = { onEvent(SettingsContentEvent.OnAutosaveChange(it)) }
            )
        }

        Text(
            text = stringResource(R.string.settings_autosave_prompt_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
    }
}
