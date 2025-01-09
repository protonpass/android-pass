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

package proton.android.pass.features.attachments.storagefull.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.common.api.FileSizeUtil.toHumanReadableSize
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.attachments.R
import proton.android.pass.features.attachments.storagefull.presentation.StorageFullState
import me.proton.core.presentation.R as CoreR

@Composable
fun StorageFullContent(
    modifier: Modifier = Modifier,
    state: StorageFullState,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        if (isNightMode()) {
            Image.Default(id = R.drawable.storage_full_night)
        } else {
            Image.Default(id = R.drawable.storage_full)
        }
        when (state) {
            StorageFullState.Loading -> {}
            is StorageFullState.Success ->
                StorageConsumed(
                    state = state,
                    onClick = onClick
                )
        }

        Text.Headline(stringResource(R.string.storage_full_title))
        Text.Body1Weak(stringResource(R.string.storage_full_body))
        Button.Circular(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .fillMaxWidth(),
            color = PassTheme.colors.interactionNormMajor2,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onClick() }
        ) {
            Text.Body1Regular(stringResource(R.string.storage_full_upgrade))
        }
    }
}

@Composable
private fun StorageConsumed(
    modifier: Modifier = Modifier,
    state: StorageFullState.Success,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        val used = remember(state.used) {
            toHumanReadableSize(state.used)
        }
        val quota = remember(state.quota) {
            toHumanReadableSize(state.quota)
        }
        Text.Body2Weak(
            modifier = Modifier.clickable { onClick() },
            text = stringResource(R.string.storage_full_amount, used, quota),
            color = PassTheme.colors.signalDanger
        )
        Icon.Default(
            modifier = Modifier.size(20.dp),
            id = CoreR.drawable.ic_proton_exclamation_circle_filled,
            tint = PassTheme.colors.signalDanger
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StorageFullContentDarkPreview() {
    PassTheme(isDark = true) {
        Surface {
            StorageFullContent(
                onClick = {},
                state = StorageFullState.Success(used = 1000, quota = 1000)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun StorageFullContentPreview() {
    PassTheme(isDark = false) {
        Surface {
            StorageFullContent(
                onClick = {},
                state = StorageFullState.Success(used = 1000, quota = 1000)
            )
        }
    }
}
