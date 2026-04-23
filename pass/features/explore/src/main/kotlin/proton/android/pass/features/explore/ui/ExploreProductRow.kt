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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.explore.R
import proton.android.pass.protonapps.api.ProtonAppType
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ExploreProductRow(
    type: ProtonAppType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon.Default(
            modifier = Modifier.size(32.dp),
            id = type.iconRes(),
            tint = Color.Unspecified
        )
        Column(modifier = Modifier.weight(1f)) {
            Text.Body1Medium(text = stringResource(type.titleRes()))
            Text.Body2Weak(text = stringResource(type.subtitleRes()))
        }
        Icon.Default(
            modifier = Modifier.size(20.dp),
            id = CoreR.drawable.ic_proton_chevron_right,
            tint = PassTheme.colors.textWeak
        )
    }
}

@DrawableRes
private fun ProtonAppType.iconRes(): Int = when (this) {
    ProtonAppType.Vpn -> CoreR.drawable.ic_logo_vpn_no_bg
    ProtonAppType.Mail -> CoreR.drawable.ic_logo_mail_no_bg
    ProtonAppType.Drive -> CoreR.drawable.ic_logo_drive_no_bg
    ProtonAppType.Lumo -> R.drawable.ic_logo_lumo_no_bg
    ProtonAppType.Meet -> R.drawable.ic_logo_meet_no_bg
}

@StringRes
private fun ProtonAppType.titleRes(): Int = when (this) {
    ProtonAppType.Vpn -> R.string.explore_product_vpn_title
    ProtonAppType.Mail -> R.string.explore_product_mail_title
    ProtonAppType.Drive -> R.string.explore_product_drive_title
    ProtonAppType.Lumo -> R.string.explore_product_lumo_title
    ProtonAppType.Meet -> R.string.explore_product_meet_title
}

@StringRes
private fun ProtonAppType.subtitleRes(): Int = when (this) {
    ProtonAppType.Vpn -> R.string.explore_product_vpn_subtitle
    ProtonAppType.Mail -> R.string.explore_product_mail_subtitle
    ProtonAppType.Drive -> R.string.explore_product_drive_subtitle
    ProtonAppType.Lumo -> R.string.explore_product_lumo_subtitle
    ProtonAppType.Meet -> R.string.explore_product_meet_subtitle
}

@Preview
@Composable
internal fun ExploreProductRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ExploreProductRow(
                type = ProtonAppType.Vpn,
                onClick = {}
            )
        }
    }
}
