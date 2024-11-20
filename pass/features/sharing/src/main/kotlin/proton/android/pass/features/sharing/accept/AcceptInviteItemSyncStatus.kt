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

package proton.android.pass.features.sharing.accept

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text

private const val INVITE_PROGRESS_LABEL = "Invite progress"

@Composable
internal fun AcceptInviteItemSyncStatus(
    modifier: Modifier = Modifier,
    downloaded: Int,
    total: Int
) {
    val progress = remember(downloaded) {
        if (total == 0) {
            0f
        } else {
            downloaded.toFloat() / total.toFloat()
        }
    }

    val inviteProgress by animateFloatAsState(
        targetValue = progress,
        label = INVITE_PROGRESS_LABEL
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            progress = inviteProgress,
            color = PassTheme.colors.interactionNormMajor2,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            strokeCap = StrokeCap.Round
        )

        Text.Body1Regular(
            text = "$downloaded ${SpecialCharacters.SLASH} $total"
        )
    }
}

@[Preview Composable]
internal fun AcceptInviteItemSyncStatusPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AcceptInviteItemSyncStatus(downloaded = 3, total = 10)
        }
    }
}
