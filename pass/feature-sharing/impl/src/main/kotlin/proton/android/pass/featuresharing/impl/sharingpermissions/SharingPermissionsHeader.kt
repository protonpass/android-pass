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

package proton.android.pass.featuresharing.impl.sharingpermissions

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featuresharing.impl.R

@Composable
fun SharingPermissionsHeader(
    modifier: Modifier = Modifier,
    state: SharingPermissionsHeaderState,
    onSetAllClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = pluralStringResource(
                id = R.plurals.sharing_member_count,
                count = state.memberCount,
                state.memberCount
            ),
            color = ProtonTheme.colors.textWeak
        )

        Row(
            modifier = Modifier
                .border(
                    border = BorderStroke(
                        width = ButtonDefaults.OutlinedBorderSize,
                        color = PassTheme.colors.interactionNorm
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onSetAllClick() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.share_permissions_title),
                color = PassTheme.colors.interactionNorm
            )

            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                tint = PassTheme.colors.interactionNorm,
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun SharingPermissionsHeaderPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SharingPermissionsHeader(
                state = SharingPermissionsHeaderState(
                    memberCount = 2
                ),
                onSetAllClick = {}
            )
        }
    }
}
