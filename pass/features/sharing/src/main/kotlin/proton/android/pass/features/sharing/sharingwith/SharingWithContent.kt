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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SharingWithContent(
    modifier: Modifier = Modifier,
    state: SharingWithUIState,
    editingEmail: String,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onEvent: (SharingWithUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    val focusRequester = remember { FocusRequester() }

    var parentHeight: Dp by remember { mutableStateOf(Dp.Unspecified) }

    val density = LocalDensity.current

    val errorMessageRes = remember(state.errorMessage) {
        when (state.errorMessage) {
            ErrorMessage.NoAddressesCanBeInvited -> {
                R.string.sharing_with_no_addresses_can_be_invited
            }

            ErrorMessage.SomeAddressesCannotBeInvited -> {
                R.string.sharing_with_some_addresses_cannot_be_invited
            }

            ErrorMessage.EmailNotValid -> {
                R.string.share_with_email_error
            }

            ErrorMessage.CannotInviteOutsideOrg -> {
                R.string.share_with_email_cannot_invite_outside_org
            }

            ErrorMessage.EmailAlreadyAdded -> {
                R.string.share_with_email_email_already_added
            }

            ErrorMessage.None -> null
        }
    }

    LaunchedEffect(state.scrollToBottom) {
        if (state.scrollToBottom) {
            scrollState.animateScrollTo(scrollState.maxValue)
            onEvent(SharingWithUiEvent.OnScrolledToBottom)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(R.string.share_with_title),
                onUpClick = { onNavigateEvent(SharingNavigation.BackToHome) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = state.isLoading,
                        buttonEnabled = state.isContinueEnabled,
                        color = if (state.isContinueEnabled) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor1
                        },
                        text = {
                            Text.Body2Regular(
                                text = stringResource(id = CompR.string.action_continue),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(SharingWithUiEvent.ContinueClick) }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.medium)
                .onSizeChanged { parentHeight = with(density) { it.height.toDp() } },
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
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
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
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
                isError = errorMessageRes != null,
                errorMessage = errorMessageRes?.let { stringResource(it) }.orEmpty(),
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
    val contentColor = if (emailState.isError) {
        PassTheme.colors.passwordInteractionNormMajor2
    } else {
        PassTheme.colors.textNorm
    }

    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = Radius.small))
            .applyIf(
                condition = emailState.isError,
                ifTrue = { background(color = PassTheme.colors.passwordInteractionNormMinor1) },
                ifFalse = { background(color = PassTheme.colors.interactionNormMinor1) }
            )
            .clickable { onClick() }
            .padding(all = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Text.Body2Regular(
            text = emailState.email,
            color = contentColor
        )

        if (isSelected) {
            Icon(
                modifier = Modifier.size(size = Spacing.mediumSmall),
                painter = painterResource(id = CoreR.drawable.ic_proton_cross),
                contentDescription = stringResource(R.string.share_with_remove_email_content_description),
                tint = contentColor
            )
        }
    }
}

@[Preview Composable]
internal fun SharingWithChipPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isSelected) = input

    PassTheme(isDark = isDark) {
        Surface {
            SharingWithChip(
                emailState = EnteredEmailState(
                    email = "some@email.test",
                    isError = isSelected
                ),
                isSelected = isSelected,
                onClick = {}
            )
        }
    }
}

private const val RATIO_HEIGHT_EMAIL_LIST = 0.3f
