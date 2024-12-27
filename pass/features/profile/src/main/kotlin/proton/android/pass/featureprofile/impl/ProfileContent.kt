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

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton
import proton.android.pass.featureprofile.impl.accountswitcher.AccountSwitcherList
import proton.android.pass.features.profile.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ProtonTopAppBar(
                    backgroundColor = PassTheme.colors.backgroundStrong,
                    title = {
                        Text(
                            text = stringResource(R.string.profile_screen_title),
                            style = PassTheme.typography.heroNorm()
                        )
                    },
                    actions = {
                        if (state.showUpgradeButton) {
                            UpgradeButton(
                                modifier = Modifier.padding(horizontal = Spacing.mediumSmall),
                                onUpgradeClick = { onEvent(ProfileUiEvent.OnUpgradeClick) }
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(PassTheme.colors.backgroundStrong)
                    .padding(padding)
            ) {
                if (state.accounts.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(Spacing.medium),
                        text = stringResource(R.string.profile_option_account),
                        style = ProtonTheme.typography.defaultSmallWeak
                    )
                    AccountSwitcherList(
                        isExpanded = isExpanded,
                        accountItemList = state.accounts,
                        onExpandedChange = { isExpanded = it },
                        onEvent = {
                            onEvent(it)
                            isExpanded = false
                        }
                    )
                }

                ItemSummary(
                    modifier = Modifier.padding(
                        horizontal = Spacing.none,
                        vertical = Spacing.medium
                    ),
                    itemSummaryUiState = state.itemSummaryUiState,
                    onEvent = onEvent
                )

                Column(
                    modifier = Modifier.padding(all = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
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

                    if (state.isSimpleLoginAliasesSyncEnabled || state.isAdvancedAliasManagementEnabled) {
                        ProfileAliasesSection(
                            onclick = { onEvent(ProfileUiEvent.OnAliasesClicked) }
                        )
                    }

                    if (state.passkeySupport is ProfilePasskeySupportSection.Show) {
                        PasskeyProfileSection(
                            support = state.passkeySupport
                        )
                    }

                    ProfileSecureLinksSection(
                        shouldShowPlusIcon = state.showUpgradeButton,
                        secureLinksCount = state.secureLinksCount,
                        onClick = { onEvent(ProfileUiEvent.OnSecureLinksClicked) }
                    )

                    AccountProfileSection(
                        planInfo = state.accountType,
                        onSettingsClick = { onEvent(ProfileUiEvent.OnSettingsClick) }
                    )

                    HelpCenterProfileSection(
                        onFeedbackClick = { onEvent(ProfileUiEvent.OnFeedbackClick) },
                        onImportExportClick = { onEvent(ProfileUiEvent.OnImportExportClick) },
                        onRateAppClick = { onEvent(ProfileUiEvent.OnRateAppClick) },
                        onTutorialClick = { onEvent(ProfileUiEvent.OnTutorialClick) }
                    )

                    Box(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onEvent(ProfileUiEvent.OnCopyAppVersionClick) },
                                onLongClick = { onEvent(ProfileUiEvent.OnAppVersionLongClick) }
                            )
                            .fillMaxWidth()
                            .padding(all = Spacing.large),
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
        Scrim(
            onDismiss = { },
            visible = isExpanded
        )
    }
}


@Composable
fun Scrim(onDismiss: () -> Unit, visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = TweenSpec(),
        label = "scrim"
    )
    val dismissModifier = if (visible) {
        Modifier.pointerInput(Unit) { detectTapGestures { onDismiss() } }
    } else {
        Modifier
    }
    val color = PassTheme.colors.backdrop
    Canvas(
        Modifier
            .fillMaxSize()
            .then(dismissModifier)
    ) {
        drawRect(color = color, alpha = alpha)
    }
}

