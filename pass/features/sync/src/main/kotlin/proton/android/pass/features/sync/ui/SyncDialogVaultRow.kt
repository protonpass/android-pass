/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.features.sync.R
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun SyncDialogVaultRow(
    modifier: Modifier = Modifier,
    name: String,
    hasSyncFinished: Boolean,
    itemCurrent: Int?,
    itemTotal: Int?,
    color: ShareColor,
    icon: ShareIcon,
    hasSyncFailed: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = color.toColor(isBackground = true),
            icon = icon.toResource(),
            iconColor = color.toColor()
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = ProtonTheme.typography.defaultSmallNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val subtitle = when {
                itemTotal == 0 -> stringResource(id = R.string.sync_item_empty)

                !hasSyncFinished && itemTotal != null && itemCurrent == itemTotal ->
                    pluralStringResource(R.plurals.sync_item_count, itemTotal, itemTotal)

                hasSyncFinished && itemTotal != null && itemCurrent == itemTotal ->
                    pluralStringResource(R.plurals.sync_finished_item_count, itemTotal, itemTotal)

                hasSyncFailed -> stringResource(id = R.string.sync_item_failed)

                itemCurrent != null && itemTotal != null ->
                    stringResource(R.string.sync_progress_item_count, itemCurrent, itemTotal)

                else -> stringResource(R.string.sync_item_not_started)
            }
            Text(
                text = subtitle,
                style = PassTheme.typography.body3Weak(),
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier.weight(LOADING_ROW_WEIGHT),
            contentAlignment = Alignment.Center
        ) {
            when {

                hasSyncFailed -> {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_proton_cross_big),
                        tint = PassTheme.colors.signalDanger,
                        contentDescription = null
                    )
                }

                hasSyncFinished || itemCurrent != null && itemCurrent == itemTotal -> {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_proton_checkmark),
                        tint = PassTheme.colors.interactionNormMajor1,
                        contentDescription = null
                    )
                }

                else -> {
                    Loading(
                        modifier = Modifier.size(24.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

private const val LOADING_ROW_WEIGHT = 0.2f

@Preview
@Composable
internal fun SyncVaultLoadingRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            Column {
                SyncDialogVaultRow(
                    name = "vault name",
                    itemCurrent = 1,
                    itemTotal = 4,
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    hasSyncFailed = false,
                    hasSyncFinished = false
                )
            }
        }
    }
}

@Preview
@Composable
internal fun SyncVaultNotLoadingRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            Column {
                SyncDialogVaultRow(
                    name = "vault name",
                    itemCurrent = 4,
                    itemTotal = 4,
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    hasSyncFailed = false,
                    hasSyncFinished = false
                )
            }
        }
    }
}
