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

package proton.android.pass.features.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import me.proton.core.presentation.R as CoreR

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    state: AuthStateContent,
    canLogout: Boolean,
    onEvent: (AuthUiEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSubmit = {
        keyboardController?.hide()
        val hasExtraPassword = state.showExtraPassword.getOrNull() ?: false
        onEvent(AuthUiEvent.OnPasswordSubmit(hasExtraPassword))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            ProtonTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {},
                navigationIcon = {
                    if (state.showBackNavigation) {
                        BackArrowCircleIconButton(
                            modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                            color = PassTheme.colors.interactionNorm,
                            backgroundColor = PassTheme.colors.interactionNormMinor1,
                            onUpClick = { onEvent(AuthUiEvent.OnNavigateBack) }
                        )
                    }
                },
                actions = {
                    if (canLogout && state.showLogout) {
                        IconButton(
                            onClick = { onEvent(AuthUiEvent.OnSignOut) }
                        ) {
                            Icon(
                                painter = painterResource(CoreR.drawable.ic_proton_arrow_out_from_rectangle),
                                contentDescription = stringResource(CoreR.string.presentation_menu_item_title_sign_out)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Spacing.medium)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = Spacing.medium)
            ) {
                AuthScreenHeader(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                AuthScreenMasterPasswordForm(
                    state = state,
                    onEvent = onEvent,
                    onSubmit = onSubmit
                )
            }

            LoadingCircleButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.medium),
                buttonHeight = 40.dp,
                text = {
                    Text(
                        text = stringResource(R.string.auth_unlock_button),
                        color = PassTheme.colors.textInvert
                    )
                },
                color = PassTheme.colors.interactionNormMajor2,
                isLoading = state.isLoadingState.value(),
                onClick = onSubmit
            )
        }
    }
}
