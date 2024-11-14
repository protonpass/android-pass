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

package proton.android.pass.features.sharing.sharingpermissions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.sharing.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SharingPermissionsHeader(
    modifier: Modifier = Modifier,
    memberCount: Int,
    onSetAllClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text.Body2Medium(
            text = "${stringResource(R.string.sharing_member_count_header)} ($memberCount)",
            color = PassTheme.colors.textWeak
        )

        Row(
            modifier = Modifier
                .border(
                    border = BorderStroke(
                        width = ButtonDefaults.OutlinedBorderSize,
                        color = PassTheme.colors.interactionNormMajor2
                    ),
                    shape = RoundedCornerShape(size = Radius.small)
                )
                .clickable { onSetAllClick() }
                .padding(
                    start = Spacing.mediumSmall,
                    top = Spacing.extraSmall,
                    end = Spacing.small,
                    bottom = Spacing.extraSmall
                ),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text.Body3Regular(
                text = stringResource(id = R.string.share_permissions_title),
                color = PassTheme.colors.interactionNormMajor2
            )

            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_chevron_tiny_down),
                tint = PassTheme.colors.interactionNormMajor2,
                contentDescription = null
            )
        }
    }
}

@[Preview Composable]
internal fun SharingPermissionsHeaderPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SharingPermissionsHeader(
                memberCount = 2,
                onSetAllClick = {}
            )
        }
    }
}
