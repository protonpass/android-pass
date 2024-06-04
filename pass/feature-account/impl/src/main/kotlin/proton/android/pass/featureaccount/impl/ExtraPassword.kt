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

package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption

@Composable
internal fun ExtraPassword(modifier: Modifier = Modifier, onEvent: (AccountContentEvent) -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        ColorSettingOption(
            modifier = Modifier.roundedContainerNorm(),
            innerModifier = Modifier.padding(vertical = Spacing.medium),
            text = stringResource(R.string.account_settings_list_item_extra_password_header),
            textColor = PassTheme.colors.interactionNormMajor2,
            iconBgColor = PassTheme.colors.interactionNormMinor1,
            onClick = { onEvent(AccountContentEvent.SetExtraPassword) }
        )
        Text(
            text = stringResource(R.string.account_settings_list_item_extra_password_description),
            style = PassTheme.typography.body3Weak()
        )
    }
}
