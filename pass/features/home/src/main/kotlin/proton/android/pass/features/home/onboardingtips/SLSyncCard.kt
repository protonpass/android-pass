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

package proton.android.pass.features.home.onboardingtips

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.features.home.R

@Composable
fun SLSyncCard(
    modifier: Modifier = Modifier,
    aliasCount: Int,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    SpotlightCard(
        modifier = modifier,
        backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
        title = stringResource(id = R.string.sl_sync_banner_title),
        body = pluralStringResource(id = R.plurals.sl_sync_banner_text, aliasCount, aliasCount),
        buttonText = stringResource(id = R.string.sl_sync_banner_settings),
        caption = stringResource(id = R.string.sl_sync_banner_note),
        titleColor = PassTheme.colors.textNorm,
        image = { Image.Default(R.drawable.spotlight_sl_sync) },
        onClick = onClick,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun SLSyncCardPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SLSyncCard(
                aliasCount = 1,
                onClick = {},
                onDismiss = {}
            )
        }
    }
}
