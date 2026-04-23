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

package proton.android.pass.features.explore.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.features.explore.R

@Composable
internal fun ProtonForBusinessBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                if (LocalDark.current) {
                    R.drawable.proton_business_dark
                } else {
                    R.drawable.proton_business_light
                }
            ),
            contentDescription = stringResource(R.string.explore_business_banner_title)
        )

        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier
        ) {
            Image(
                painter = painterResource(
                    if (LocalDark.current) {
                        R.drawable.proton_business_cover_dark
                    } else {
                        R.drawable.proton_business_cover_light
                    }
                ),
                contentDescription = stringResource(R.string.explore_business_banner_title),
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            PassCircleButton(
                text = stringResource(R.string.explore_business_banner_cta),
                onClick = onClick,
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Preview
@Composable
internal fun ProtonForBusinessBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ProtonForBusinessBanner(onClick = {})
        }
    }
}
