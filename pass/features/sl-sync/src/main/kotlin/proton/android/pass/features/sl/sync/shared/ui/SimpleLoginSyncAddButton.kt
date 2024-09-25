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

package proton.android.pass.features.sl.sync.shared.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncAddButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    LoadingCircleButton(
        modifier = modifier,
        isLoading = false,
        color = PassTheme.colors.interactionNormMinor1,
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                painter = painterResource(R.drawable.ic_proton_plus),
                contentDescription = null,
                tint = PassTheme.colors.interactionNormMajor2
            )
        },
        text = {
            Text(
                text = stringResource(id = CompR.string.action_add),
                color = PassTheme.colors.interactionNormMajor2,
                style = ProtonTheme.typography.captionNorm
            )
        },
        onClick = onClick
    )
}
