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

package proton.android.pass.features.sharing.sharingsummary

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.ShareType
import proton.android.pass.features.sharing.R

@Composable
internal fun SharingSummaryAccessLevelSection(modifier: Modifier = Modifier, shareType: ShareType) {

    val rowsResIds = remember(shareType) {
        when (shareType) {
            ShareType.Vault -> listOf(
                R.string.sharing_can_view to R.string.sharing_can_view_description,
                R.string.sharing_can_edit to R.string.sharing_can_edit_description,
                R.string.sharing_can_manage to R.string.sharing_can_manage_description
            )

            ShareType.Item -> listOf(
                R.string.sharing_can_view to R.string.sharing_bottomsheet_item_viewer_subtitle,
                R.string.sharing_can_edit to R.string.sharing_bottomsheet_item_editor_subtitle,
                R.string.sharing_can_manage to R.string.sharing_bottomsheet_item_admin_subtitle
            )
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Text.Body1Regular(
            modifier = Modifier.padding(bottom = Spacing.small),
            text = stringResource(id = R.string.share_summary_access_level_title),
            color = PassTheme.colors.textWeak
        )

        rowsResIds.forEach { (titleResId, subtitleResId) ->
            SharingSummaryAccessLevelRow(
                titleResId = titleResId,
                subtitleResId = subtitleResId
            )
        }
    }
}

@Composable
private fun SharingSummaryAccessLevelRow(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    @StringRes subtitleResId: Int
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .roundedContainer(
                backgroundColor = PassTheme.colors.backgroundNorm,
                borderColor = PassTheme.colors.inputBorderNorm
            )
            .padding(all = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        Text.Body1Regular(
            text = stringResource(id = titleResId)
        )

        Text.Body2Regular(
            text = stringResource(id = subtitleResId),
            color = PassTheme.colors.textWeak
        )
    }
}

@[Preview Composable]
internal fun SharingSummaryAccessLevelSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SharingSummaryAccessLevelSection(
                shareType = ShareType.Item
            )
        }
    }
}
