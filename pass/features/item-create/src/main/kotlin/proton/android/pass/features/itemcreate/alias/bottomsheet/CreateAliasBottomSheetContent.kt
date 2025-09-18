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

package proton.android.pass.features.itemcreate.alias.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton
import proton.android.pass.composecomponents.impl.container.AnimatedVisibilityWithOnComplete
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.container.rememberAnimatedVisibilityState
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.AliasAdvancedOptionsSection
import proton.android.pass.features.itemcreate.alias.AliasDraftSavedState
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.AliasOptionsUiModel
import proton.android.pass.features.itemcreate.alias.AliasPrefixSuffixText
import proton.android.pass.features.itemcreate.alias.AliasSuffixUiModel
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.BaseAliasUiState
import proton.android.pass.features.itemcreate.alias.CloseScreenEvent
import proton.android.pass.features.itemcreate.alias.MailboxSection
import proton.android.pass.features.itemcreate.common.AliasItemValidationError

@Composable
internal fun CreateAliasBottomSheetContent(
    modifier: Modifier = Modifier,
    state: BaseAliasUiState,
    aliasItemFormState: AliasItemFormState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onPrefixChanged: (String) -> Unit,
    onNavigate: (BaseAliasNavigation) -> Unit,
    showAdvancedOptionsInitially: Boolean = false
) {
    val isBlankAliasError = state.errorList.contains(AliasItemValidationError.BlankPrefix)
    val isInvalidAliasError =
        state.errorList.contains(AliasItemValidationError.InvalidAliasContent)

    var showAdvancedOptions by rememberSaveable { mutableStateOf(showAdvancedOptionsInitially) }
    val visibilityState = rememberAnimatedVisibilityState(initialState = true)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BottomSheetTitle(title = stringResource(R.string.field_alias_you_are_about_to_create))
        AliasPrefixSuffixText(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
            prefix = aliasItemFormState.prefix,
            suffix = aliasItemFormState.selectedSuffix?.suffix ?: "",
            suffixColor = PassTheme.colors.loginInteractionNormMajor2,
            fontSize = 20.sp
        )
        AnimatedVisibility(visible = showAdvancedOptions) {
            AliasAdvancedOptionsSection(
                enabled = true,
                isBottomSheet = true,
                prefix = aliasItemFormState.prefix,
                suffix = aliasItemFormState.selectedSuffix,
                isError = isBlankAliasError || isInvalidAliasError,
                canSelectSuffix = aliasItemFormState.aliasOptions.suffixes.size > 1,
                onPrefixChanged = onPrefixChanged,
                onSuffixClicked = { onNavigate(BaseAliasNavigation.SelectSuffix) }
            )
        }
        AnimatedVisibilityWithOnComplete(
            visibilityState = visibilityState,
            onComplete = { showAdvancedOptions = true }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShowAdvancedOptionsButton(
                    currentValue = showAdvancedOptions,
                    onClick = { visibilityState.toggle() }
                )
            }
        }
        MailboxSection(
            isBottomSheet = true,
            selectedMailboxes = aliasItemFormState.selectedMailboxes.toImmutableList(),
            isCreateMode = false,
            isEditAllowed = aliasItemFormState.aliasOptions.mailboxes.size > 1,
            isLoading = state.isLoadingState.value(),
            onMailboxClick = { onNavigate(BaseAliasNavigation.SelectMailbox) }
        )
        AnimatedVisibility(visible = state.hasReachedAliasLimit) {
            InfoBanner(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.create_alias_content_limit_banner),
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1
            )
        }
        BottomSheetCancelConfirm(
            modifier = Modifier.padding(top = 36.dp),
            isLoading = state.isLoadingState == IsLoadingState.Loading,
            showUpgrade = state.hasReachedAliasLimit,
            onCancel = onCancel,
            onConfirm = onConfirm,
            onUpgradeClick = { onNavigate(BaseAliasNavigation.Upgrade) }
        )
    }
}

@Preview
@Composable
fun CreateAliasBottomSheetContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CreateAliasBottomSheetContent(
                state = BaseAliasUiState(
                    isDraft = false,
                    errorList = emptySet(),
                    isLoadingState = IsLoadingState.NotLoading,
                    itemSavedState = ItemSavedState.Unknown,
                    isAliasDraftSavedState = AliasDraftSavedState.Unknown,
                    isApplyButtonEnabled = IsButtonEnabled.Enabled,
                    closeScreenEvent = CloseScreenEvent.NotClose,
                    hasUserEditedContent = false,
                    hasReachedAliasLimit = false,
                    canUpgrade = false,
                    isCustomTypeEnabled = false,
                    displayFileAttachmentsOnboarding = false,
                    displayAdvancedOptionsBanner = false,
                    attachmentsState = AttachmentsState.Initial,
                    canPerformPaidAction = true,
                    focusedField = None
                ),
                aliasItemFormState = AliasItemFormState(
                    title = "some title",
                    prefix = "some alias",
                    note = "",
                    customFields = emptyList(),
                    aliasOptions = AliasOptionsUiModel(
                        suffixes = listOf(
                            AliasSuffixUiModel(
                                suffix = ".some@suffix.test",
                                signedSuffix = "",
                                isCustom = false,
                                isPremium = false,
                                domain = ""
                            )
                        ),
                        mailboxes = listOf(
                            AliasMailboxUiModel(
                                id = 1,
                                email = "some.mailbox@test.local"
                            ),
                            AliasMailboxUiModel(
                                id = 2,
                                email = "other.mailbox@test.local"
                            )
                        )
                    ),
                    selectedSuffix = AliasSuffixUiModel(
                        suffix = ".some@suffix.test",
                        signedSuffix = "",
                        isCustom = false,
                        isPremium = false,
                        domain = ""
                    ),
                    selectedMailboxes = setOf(
                        AliasMailboxUiModel(
                            id = 1,
                            email = "some.mailbox@test.local"
                        )
                    )
                ),
                showAdvancedOptionsInitially = true,
                onCancel = {},
                onConfirm = {},
                onPrefixChanged = {},
                onNavigate = {}
            )
        }
    }
}
