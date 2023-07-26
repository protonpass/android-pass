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
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

@Composable
fun SummarySection(modifier: Modifier = Modifier, state: SharingSummaryUIState) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.share_summary_title),
            style = PassTypography.hero
        )
        SharingSummaryDescription(state = state)
    }
}

@Preview
@Composable
fun SummarySectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SummarySection(
                state = SharingSummaryUIState(
                    email = "myemail@proton.me",
                    vaultWithItemCount = VaultWithItemCount(
                        vault = Vault(
                            shareId = ShareId(id = "eloquentiam"),
                            name = "My Vault",
                            color = ShareColor.Color1,
                            icon = ShareIcon.Icon1,
                            isPrimary = false
                        ),
                        activeItemCount = 34,
                        trashedItemCount = 21
                    ),
                    sharingType = SharingType.Read
                )
            )
        }
    }
}
