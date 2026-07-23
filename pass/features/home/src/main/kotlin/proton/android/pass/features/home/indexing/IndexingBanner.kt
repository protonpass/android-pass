/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG.
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

package proton.android.pass.features.home.indexing

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.loading.ProgressWithLabel
import proton.android.pass.data.api.repositories.IndexingStatus
import proton.android.pass.features.home.R

@Composable
internal fun IndexingBanner(modifier: Modifier = Modifier, indexingStatus: IndexingStatus) {
    val (label, progress) = when (indexingStatus) {
        is IndexingStatus.InProgress -> stringResource(
            R.string.home_indexing_banner_progress,
            indexingStatus.current,
            indexingStatus.total
        ) to indexingStatus.current.toFloat() / indexingStatus.total
        else -> stringResource(R.string.home_indexing_banner) to null
    }

    ProgressWithLabel(
        modifier = modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
        label = label,
        progress = progress
    )
}

internal class IndexingStatusPreviewProvider : PreviewParameterProvider<IndexingStatus> {
    override val values: Sequence<IndexingStatus> = sequenceOf(
        IndexingStatus.Indexing,
        IndexingStatus.InProgress(current = 0, total = 15_000),
        IndexingStatus.InProgress(current = 4_500, total = 15_000),
        IndexingStatus.InProgress(current = 14_999, total = 15_000)
    )
}

internal class ThemedIndexingStatusPreviewProvider :
    ThemePairPreviewProvider<IndexingStatus>(IndexingStatusPreviewProvider())

@[Preview Composable]
internal fun IndexingBannerPreview(
    @PreviewParameter(ThemedIndexingStatusPreviewProvider::class) input: Pair<Boolean, IndexingStatus>
) {
    PassTheme(isDark = input.first) {
        Surface {
            IndexingBanner(indexingStatus = input.second)
        }
    }
}
