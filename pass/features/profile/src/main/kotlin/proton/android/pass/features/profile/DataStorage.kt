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

package proton.android.pass.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.FileSizeUtil.toHumanReadableSize
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun DataStorage(modifier: Modifier = Modifier, state: DataStorageState) {
    if (state.shouldDisplay) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text.Body2Weak(stringResource(R.string.profile_storage_data_title))
            val used = remember(state.used) {
                toHumanReadableSize(state.used)
            }
            val quota = remember(state.quota) {
                toHumanReadableSize(state.quota)
            }
            val percentage = remember(state.used, state.quota) {
                if (state.quota > 0) {
                    state.used.coerceAtLeast(0).toFloat() / state.quota.coerceAtLeast(1).toFloat() * 100
                } else {
                    0f
                }.coerceIn(0f, 100f)
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                val amount = stringResource(R.string.profile_storage_amount, used, quota)
                Text.Body2Weak("$amount ($percentage%)")
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    progress = percentage / 100,
                    color = PassTheme.colors.signalSuccess,
                    backgroundColor = PassTheme.colors.signalSuccess.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Preview
@Composable
fun DataStoragePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            DataStorage(state = DataStorageState(shouldDisplay = true, used = 100, quota = 1000))
        }
    }
}
