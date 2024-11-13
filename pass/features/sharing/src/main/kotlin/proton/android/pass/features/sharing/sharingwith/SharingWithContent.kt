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

package proton.android.pass.features.sharing.sharingwith

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation
import me.proton.core.presentation.R as CoreR

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SharingWithContent(
    modifier: Modifier = Modifier,
    state: SharingWithUIState,
    editingEmail: String,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onEvent: (SharingWithUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.BackToHome) },
                actions = {
                    LoadingCircleButton(
                        buttonEnabled = state.isContinueEnabled,
                        color = if (state.isContinueEnabled) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor1
                        },
                        onClick = { onEvent(SharingWithUiEvent.ContinueClick) },
                        isLoading = state.isLoading,
                        text = {
                            Text(
                                text = stringResource(R.string.share_continue),
                                style = PassTheme.typography.body3Norm(),
                                color = PassTheme.colors.textInvert
                            )
                        }
                    )
                }
            )
        }
    ) { padding ->

        val scrollState = rememberScrollState()
        LaunchedEffect(state.scrollToBottom) {
            if (state.scrollToBottom) {
                scrollState.animateScrollTo(scrollState.maxValue)
                onEvent(SharingWithUiEvent.OnScrolledToBottom)
            }
        }

        var parentHeight: Dp by remember { mutableStateOf(Dp.Unspecified) }
        val density = LocalDensity.current
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.medium)
                .onSizeChanged { parentHeight = with(density) { it.height.toDp() } },
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.share_with_title),
                style = PassTheme.typography.heroNorm()
            )

            if (state.showEditVault && state.vault != null) {
                CustomizeVault(
                    vault = state.vault,
                    onClick = {
                        onNavigateEvent(SharingNavigation.EditVault(shareId = state.vault.shareId))
                    }
                )
            }

            Box(
                modifier = Modifier
                    .heightIn(min = 0.dp, max = parentHeight * RATIO_HEIGHT_EMAIL_LIST)
                    .verticalScroll(scrollState)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    state.enteredEmails.forEachIndexed { idx, emailState ->
                        SharingWithChip(
                            emailState = emailState,
                            isSelected = state.selectedEmailIndex.value() == idx,
                            onClick = {
                                onEvent(SharingWithUiEvent.EmailClick(idx))
                            }
                        )
                    }
                }
            }

            val focusRequester = remember { FocusRequester() }

            val (isError, errorMessage) = when (state.errorMessage) {
                ErrorMessage.NoAddressesCanBeInvited -> {
                    true to stringResource(R.string.sharing_with_no_addresses_can_be_invited)
                }
                ErrorMessage.SomeAddressesCannotBeInvited -> {
                    true to stringResource(R.string.sharing_with_some_addresses_cannot_be_invited)
                }
                ErrorMessage.EmailNotValid -> {
                    true to stringResource(R.string.share_with_email_error)
                }
                ErrorMessage.CannotInviteOutsideOrg -> {
                    true to stringResource(R.string.share_with_email_cannot_invite_outside_org)
                }
                ErrorMessage.EmailAlreadyAdded -> {
                    true to stringResource(R.string.share_with_email_email_already_added)
                }
                else -> false to ""
            }

            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = editingEmail,
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.share_with_email_hint),
                        textStyle = ProtonTheme.typography.subheadlineNorm.copy(
                            color = ProtonTheme.colors.textHint
                        )
                    )
                },
                isError = isError,
                errorMessage = errorMessage,
                textStyle = ProtonTheme.typography.subheadlineNorm,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                ),
                onChange = { onEvent(SharingWithUiEvent.EmailChange(it)) },
                onDoneClick = {
                    if (!state.canOnlyPickFromSelection) {
                        onEvent(SharingWithUiEvent.EmailSubmit)
                    }
                }
            )

            RequestFocusLaunchedEffect(focusRequester)

            PassDivider()
            InviteSuggestions(
                state = state.suggestionsUIState,
                onItemClicked = { email, state ->
                    onEvent(SharingWithUiEvent.InviteSuggestionToggle(email, state))
                }
            )
        }
    }
}

@Composable
private fun SharingWithChip(
    modifier: Modifier = Modifier,
    emailState: EnteredEmailState,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(
                top = Spacing.small,
                bottom = Spacing.small,
                end = Spacing.small
            )
            .roundedContainer(
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                borderColor = if (emailState.isError) {
                    PassTheme.colors.signalDanger
                } else Color.Transparent
            )
            .clickable { onClick() }
            .padding(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emailState.email,
            style = ProtonTheme.typography.defaultNorm
        )

        if (isSelected) {
            Icon(
                modifier = Modifier.padding(start = Spacing.small),
                painter = painterResource(id = CoreR.drawable.ic_proton_cross_circle),
                tint = PassTheme.colors.textNorm,
                contentDescription = stringResource(R.string.share_with_remove_email_content_description)
            )
        }
    }
}

@Preview
@Composable
fun SharingWithChipPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            SharingWithChip(
                emailState = EnteredEmailState("some@email.test", isError = input.second),
                isSelected = input.second,
                onClick = {}
            )
        }
    }
}

private const val RATIO_HEIGHT_EMAIL_LIST = 0.2f
