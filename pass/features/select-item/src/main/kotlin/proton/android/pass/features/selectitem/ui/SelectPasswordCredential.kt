/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.selectitem.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.selectitem.R
import proton.android.pass.composecomponents.impl.R as uiR

@Composable
internal fun SelectPasswordCredential(modifier: Modifier = Modifier, onSelectAccountClick: () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .offset(y = -Spacing.extraLarge)
                .align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Image(
                painter = painterResource(id = uiR.drawable.placeholder_bound_box),
                contentDescription = null
            )

            Text.Headline(
                text = stringResource(id = R.string.select_item_save_credential_title),
                textAlign = TextAlign.Center
            )

            Text.Body1Weak(
                text = stringResource(id = R.string.select_item_save_credential_subtitle),
                textAlign = TextAlign.Center
            )
        }

        PassCircleButton(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(all = Spacing.medium),
            text = stringResource(id = R.string.select_item_save_credential_button),
            isLoading = false,
            onClick = onSelectAccountClick
        )
    }
}

@[Preview Composable]
internal fun SelectPasswordCredentialPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SelectPasswordCredential(
                onSelectAccountClick = {}
            )
        }
    }
}
