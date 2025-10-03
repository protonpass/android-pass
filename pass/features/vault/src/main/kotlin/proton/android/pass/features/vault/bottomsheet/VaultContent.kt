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

package proton.android.pass.features.vault.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.features.vault.R

@Composable
fun VaultContent(
    modifier: Modifier = Modifier,
    state: BaseVaultUiState,
    showUpgradeUi: Boolean,
    buttonText: String,
    onNameChange: (String) -> Unit,
    onColorChange: (ShareColor) -> Unit,
    onIconChange: (ShareIcon) -> Unit,
    onClose: () -> Unit,
    onButtonClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            CreateVaultBottomSheetTopBar(
                showUpgradeButton = showUpgradeUi,
                buttonText = buttonText,
                isLoading = state.isLoading.value(),
                isButtonEnabled = showUpgradeUi || state.isCreateButtonEnabled.value(),
                onCloseClick = onClose,
                onCreateClick = onButtonClick,
                onUpgradeClick = onUpgradeClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.medium)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(visible = showUpgradeUi) {
                Column {
                    InfoBanner(
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        text = stringResource(R.string.bottomsheet_cannot_create_more_vaults)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            VaultPreviewSection(
                state = state,
                onNameChange = onNameChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColorSelectionSection(
                selected = state.color,
                onColorSelected = onColorChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            IconSelectionSection(
                selected = state.icon,
                onIconSelected = onIconChange
            )
        }
    }
}
