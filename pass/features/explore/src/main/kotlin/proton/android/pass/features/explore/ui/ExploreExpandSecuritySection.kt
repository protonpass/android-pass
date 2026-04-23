/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.explore.R
import proton.android.pass.features.explore.presentation.ExploreUiEvent
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType

@Composable
internal fun ExploreExpandSecuritySection(
    apps: ImmutableList<ProtonApp>,
    onEvent: (ExploreUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return
    Column(modifier = modifier) {
        Text.Body2Bold(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(R.string.explore_section_expand_security_title)
        )
        Column(modifier = Modifier.roundedContainerNorm()) {
            apps.forEachIndexed { index, app ->
                ExploreProductRow(
                    type = app.type,
                    onClick = { onEvent(ExploreUiEvent.ProductClicked(app.type)) }
                )
                if (index < apps.lastIndex) {
                    Divider(color = PassTheme.colors.inputBorderNorm)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun ExploreExpandSecuritySectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ExploreExpandSecuritySection(
                apps = persistentListOf(
                    ProtonApp(type = ProtonAppType.Vpn, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Mail, isInstalled = true),
                    ProtonApp(type = ProtonAppType.Drive, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Lumo, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Meet, isInstalled = false)
                ),
                onEvent = {}
            )
        }
    }
}
