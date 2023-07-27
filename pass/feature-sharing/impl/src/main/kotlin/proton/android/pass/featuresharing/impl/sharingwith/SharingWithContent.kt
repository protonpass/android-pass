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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.SharingNavigation

@Composable
fun SharingWithContent(
    modifier: Modifier = Modifier,
    state: SharingWithUIState,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailSubmit: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.Back) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(12.dp, 0.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        onClick = onEmailSubmit,
                        isLoading = state.isLoading,
                        text = {
                            Text(
                                text = stringResource(R.string.share_continue),
                                style = PassTypography.body3Regular,
                                color = PassTheme.colors.textInvert
                            )
                        }
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.share_with_title),
                style = PassTypography.hero
            )
            state.vaultName?.let { name ->
                Text(
                    text = stringResource(R.string.share_with_subtitle, name),
                    style = PassTypography.body3RegularWeak
                )
            }
            val focusRequester = remember { FocusRequester() }
            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = state.email,
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.share_with_email_hint),
                        textStyle = ProtonTheme.typography.subheadlineNorm.copy(color = ProtonTheme.colors.textHint)
                    )
                },
                isError = state.emailNotValidReason != null,
                errorMessage = stringResource(R.string.share_with_email_error),
                textStyle = ProtonTheme.typography.subheadlineNorm,
                onChange = onEmailChange,
                onDoneClick = onEmailSubmit
            )
            RequestFocusLaunchedEffect(focusRequester)
        }
    }
}
