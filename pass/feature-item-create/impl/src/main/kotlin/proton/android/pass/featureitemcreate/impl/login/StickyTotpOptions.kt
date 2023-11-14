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

package proton.android.pass.featureitemcreate.impl.login

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun StickyTotpOptions(
    modifier: Modifier = Modifier,
    onPasteCode: () -> Unit,
    onScanCode: () -> Unit
) {
    val context = LocalContext.current
    val hasCamera = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    StickyImeRow(modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onPasteCode() }
                .fillMaxHeight()
                .padding(6.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_squares),
                contentDescription = null,
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
            Text(
                text = stringResource(R.string.totp_paste_code_action),
                color = PassTheme.colors.loginInteractionNormMajor2,
                style = ProtonTheme.typography.defaultNorm,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        if (hasCamera) {
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(0.dp, 9.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onScanCode() }
                    .fillMaxHeight()
                    .padding(6.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_camera),
                    contentDescription = null,
                    tint = PassTheme.colors.loginInteractionNormMajor2
                )
                Text(
                    text = stringResource(R.string.totp_scan_code_action),
                    color = PassTheme.colors.loginInteractionNormMajor2,
                    style = ProtonTheme.typography.defaultNorm,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
fun StickyTotpOptionsPreview(
    @PreviewParameter(ThemePreviewProvider::class) input: Boolean
) {
    PassTheme(isDark = input) {
        Surface {
            StickyTotpOptions(onPasteCode = {}, onScanCode = {})
        }
    }
}
