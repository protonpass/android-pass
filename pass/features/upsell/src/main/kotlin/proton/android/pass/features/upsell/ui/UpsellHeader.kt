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

package proton.android.pass.features.upsell.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm

@Composable
internal fun UpsellHeader(
    modifier: Modifier = Modifier,
    @DrawableRes imageResId: Int,
    @StringRes titleResId: Int,
    @StringRes subtitleResId: Int
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(height = Spacing.large))

        Text(
            text = stringResource(id = titleResId),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.heroNorm()
        )

        Spacer(modifier = Modifier.height(height = Spacing.small))

        Text(
            text = stringResource(id = subtitleResId),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W300,
            color = ProtonTheme.colors.textWeak
        )
    }

}
