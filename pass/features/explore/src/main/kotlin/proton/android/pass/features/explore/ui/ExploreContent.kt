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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.explore.R
import proton.android.pass.features.explore.presentation.ExploreUiEvent
import proton.android.pass.features.explore.presentation.ExploreUiState
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType

@Composable
internal fun ExploreContent(
    state: ExploreUiState,
    onEvent: (ExploreUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.statusBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                modifier = Modifier.padding(top = Spacing.medium),
                title = stringResource(R.string.explore_tab_title)
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(contentPadding)
                .padding(top = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ExploreToolsSection(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    showPasswordHealth = state.showPasswordHealth,
                    showCodes = state.showCodes,
                    onEvent = onEvent
                )
            }
            if (state.protonApps.isNotEmpty()) {
                item(key = "expand_security") {
                    ExploreExpandSecuritySection(
                        modifier = Modifier
                            .padding(horizontal = Spacing.medium)
                            .animateItem(),
                        apps = state.protonApps,
                        onEvent = onEvent
                    )
                }
            }
            if (!state.isLoading && !state.isBusinessUser) {
                item(key = "business_banner") {
                    ProtonForBusinessBanner(
                        modifier = Modifier.animateItem(),
                        onClick = { onEvent(ExploreUiEvent.BusinessBannerClicked) }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
internal fun ExploreContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        ExploreContent(
            state = ExploreUiState(
                isLoading = false,
                protonApps = persistentListOf(
                    ProtonApp(type = ProtonAppType.Vpn, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Mail, isInstalled = true),
                    ProtonApp(type = ProtonAppType.Drive, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Lumo, isInstalled = false),
                    ProtonApp(type = ProtonAppType.Meet, isInstalled = false)
                ),
                isBusinessUser = true,
                showPasswordHealth = true,
                showCodes = true
            ),
            onEvent = {}
        )
    }
}
