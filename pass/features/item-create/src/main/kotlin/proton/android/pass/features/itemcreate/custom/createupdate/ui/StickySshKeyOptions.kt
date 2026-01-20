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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.login.StickyImeRow
import me.proton.core.presentation.R as CoreR

@Composable
internal fun StickySshKeyOptions(
    modifier: Modifier = Modifier,
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    StickyImeRow(
        modifier = if (!isGenerating) {
            modifier.clickable { onClick() }
        } else {
            modifier
        }
    ) {
        if (isGenerating) {
            CircularProgressIndicator()
        } else {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_key),
                contentDescription = null,
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.ssh_key_generate_button),
                color = PassTheme.colors.loginInteractionNormMajor2,
                style = ProtonTheme.typography.defaultNorm,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun StickySshKeyOptionsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            StickySshKeyOptions(
                isGenerating = false,
                onClick = {}
            )
        }
    }
}
