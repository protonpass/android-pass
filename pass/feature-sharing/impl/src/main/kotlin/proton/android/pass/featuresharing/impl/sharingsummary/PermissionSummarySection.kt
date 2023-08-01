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

package proton.android.pass.featuresharing.impl.sharingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType

@Composable
fun PermissionSummarySection(modifier: Modifier = Modifier, sharingType: SharingType) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.share_summary_permissions_title),
            style = PassTheme.typography.body3Weak()
        )
        val (title, subtitle) = when (sharingType) {
            SharingType.Read -> stringResource(R.string.sharing_can_view) to
                stringResource(R.string.sharing_can_view_description)

            SharingType.Write -> stringResource(R.string.sharing_can_edit) to
                stringResource(R.string.sharing_can_edit_description)

            SharingType.Admin -> stringResource(R.string.sharing_can_manage) to
                stringResource(R.string.sharing_can_manage_description)
        }
        PermissionRow(title = title, subtitle = subtitle)
    }
}

@Preview
@Composable
fun PermissionSummarySectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            PermissionSummarySection(sharingType = SharingType.Read)
        }
    }
}
