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

package proton.android.pass.features.upsell.shared.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackIconButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun UpsellTopBar(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier
            .padding(start = 12.dp),
        backgroundColor = PassTheme.colors.backgroundStrong,
        title = { },
        navigationIcon = {
            CrossBackIconButton(onUpClick = onUpClick)
        }
    )
}
