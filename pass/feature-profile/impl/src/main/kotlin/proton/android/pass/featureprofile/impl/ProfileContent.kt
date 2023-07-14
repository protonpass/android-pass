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

package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        style = PassTypography.hero
                    )
                },
                actions = {
                    if (state.showUpgradeButton) {
                        UpgradeButton(onUpgradeClick = { onEvent(ProfileUiEvent.OnUpgradeClick) })
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Profile,
                accountType = state.accountType.accountType,
                onListClick = { onEvent(ProfileUiEvent.OnListClick) },
                onCreateClick = { onEvent(ProfileUiEvent.OnCreateItemClick) },
                onProfileClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(PassTheme.colors.backgroundStrong)
                .padding(padding),
        ) {
            ItemSummary(
                modifier = Modifier.padding(0.dp, 16.dp),
                itemSummaryUiState = state.itemSummaryUiState
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppLockSection(
                    appLockSectionState = state.appLockSectionState,
                    onEvent = onEvent
                )
                if (state.autofillStatus is AutofillSupportedStatus.Supported) {
                    AutofillProfileSection(
                        isChecked = state.autofillStatus.status is AutofillStatus.EnabledByOurService,
                        onClick = { onEvent(ProfileUiEvent.OnAutofillClicked(it)) }
                    )
                }
                AccountProfileSection(
                    planInfo = state.accountType,
                    onAccountClick = { onEvent(ProfileUiEvent.OnAccountClick) },
                    onSettingsClick = { onEvent(ProfileUiEvent.OnSettingsClick) }
                )
                HelpCenterProfileSection(
                    onFeedbackClick = { onEvent(ProfileUiEvent.OnFeedbackClick) },
                    onImportExportClick = { onEvent(ProfileUiEvent.OnImportExportClick) },
                    onRateAppClick = { onEvent(ProfileUiEvent.OnRateAppClick) },
                )
                Box(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onEvent(ProfileUiEvent.OnCopyAppVersionClick) },
                            onLongClick = { onEvent(ProfileUiEvent.OnAppVersionLongClick) }
                        )
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.appVersion,
                        style = ProtonTheme.typography.captionWeak
                    )
                }
            }
        }
    }
}
