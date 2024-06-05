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

package proton.android.pass.features.extrapassword.infosheet.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.container.Column
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.extrapassword.ExtraPasswordNavigation
import proton.android.pass.features.extrapassword.R

@Composable
fun ExtraPasswordInfoBottomSheet(modifier: Modifier = Modifier, onNavigate: (ExtraPasswordNavigation) -> Unit) {
    ExtraPasswordInfoContent(
        modifier = modifier.bottomSheet(),
        onNavigate = onNavigate
    )
}

@Composable
fun ExtraPasswordInfoContent(modifier: Modifier = Modifier, onNavigate: (ExtraPasswordNavigation) -> Unit) {
    Column.Centered(modifier = modifier.padding(horizontal = Spacing.medium)) {
        Image.NoDesc(R.drawable.extra_password)
        Text.Headline(stringResource(R.string.info_extra_password_title))
        Text.Body1Regular(stringResource(R.string.info_extra_password_body))
        Spacer(modifier = Modifier.height(20.dp))
        Button.CircleButton(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Spacing.medium),
            color = PassTheme.colors.interactionNormMajor2,
            onClick = { onNavigate(ExtraPasswordNavigation.Configure) },
            content = {
                Text.Body1Regular(
                    stringResource(R.string.info_extra_password_button),
                    color = PassTheme.colors.interactionNormMinor1
                )
            }
        )
    }
}

