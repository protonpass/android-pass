package proton.android.pass.featurecreateitem.impl.alias.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.alias.AliasAdvancedOptionsSection
import proton.android.pass.featurecreateitem.impl.alias.AliasDraftSavedState
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.alias.AliasItemValidationErrors
import proton.android.pass.featurecreateitem.impl.alias.AliasMailboxUiModel
import proton.android.pass.featurecreateitem.impl.alias.AliasOptionsUiModel
import proton.android.pass.featurecreateitem.impl.alias.AliasPrefixSuffixText
import proton.android.pass.featurecreateitem.impl.alias.AliasSavedState
import proton.android.pass.featurecreateitem.impl.alias.AliasSuffixUiModel
import proton.android.pass.featurecreateitem.impl.alias.CreateUpdateAliasUiState
import proton.android.pass.featurecreateitem.impl.alias.MailboxSection
import proton.android.pass.featurecreateitem.impl.alias.SelectedAliasMailboxUiModel
import proton.android.pass.featurecreateitem.impl.alias.ShowAdvancedOptionsButton
import proton.android.pass.featurecreateitem.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featurecreateitem.impl.alias.suffixes.SelectSuffixDialog

@Composable
fun CreateAliasBottomSheetContent(
    modifier: Modifier = Modifier,
    state: CreateUpdateAliasUiState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onPrefixChanged: (String) -> Unit,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    showAdvancedOptionsInitially: Boolean = false
) {
    val isBlankAliasError = state.errorList.contains(AliasItemValidationErrors.BlankAlias)
    val isInvalidAliasError =
        state.errorList.contains(AliasItemValidationErrors.InvalidAliasContent)

    var showAdvancedOptions by remember { mutableStateOf(showAdvancedOptionsInitially) }
    var showMailboxesDialog by remember { mutableStateOf(false) }
    var showSuffixDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bottomSheetPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BottomSheetTitle(title = stringResource(R.string.field_alias_you_are_about_to_create))
        AliasPrefixSuffixText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            prefix = state.aliasItem.alias,
            suffix = state.aliasItem.selectedSuffix?.suffix ?: "",
            suffixColor = PassTheme.colors.accentPurpleNorm,
            fontSize = 20.sp
        )
        AnimatedVisibility(visible = showAdvancedOptions) {
            AliasAdvancedOptionsSection(
                enabled = true,
                prefix = state.aliasItem.alias,
                suffix = state.aliasItem.selectedSuffix,
                isError = isBlankAliasError || isInvalidAliasError,
                canSelectSuffix = state.aliasItem.aliasOptions.suffixes.size > 1,
                onPrefixChanged = onPrefixChanged,
                onSuffixClicked = { showSuffixDialog = true }
            )
        }
        MailboxSection(
            mailboxes = state.aliasItem.mailboxes,
            isEditAllowed = state.aliasItem.mailboxes.size > 1,
            isLoading = state.isLoadingState.value(),
            onMailboxClick = { showMailboxesDialog = true }
        )
        if (!showAdvancedOptions) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShowAdvancedOptionsButton(
                    currentValue = showAdvancedOptions,
                    onClick = { showAdvancedOptions = true }
                )
            }
        }
        BottomSheetCancelConfirm(
            onCancel = onCancel,
            onConfirm = onConfirm
        )

        SelectMailboxesDialog(
            show = showMailboxesDialog,
            mailboxes = state.aliasItem.mailboxes,
            onMailboxesChanged = onMailboxesChanged,
            onDismiss = { showMailboxesDialog = false }
        )
        SelectSuffixDialog(
            show = showSuffixDialog,
            suffixes = state.aliasItem.aliasOptions.suffixes.toImmutableList(),
            selectedSuffix = state.aliasItem.selectedSuffix,
            onSuffixChanged = onSuffixChanged,
            onDismiss = { showSuffixDialog = false }
        )
    }
}

@Preview
@Composable
fun CreateAliasBottomSheetContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CreateAliasBottomSheetContent(
                state = CreateUpdateAliasUiState(
                    shareList = emptyList(),
                    selectedShareId = null,
                    aliasItem = AliasItem(
                        title = "some title",
                        alias = "some alias",
                        note = "",
                        mailboxTitle = "mailbox title",
                        aliasOptions = AliasOptionsUiModel(
                            suffixes = listOf(
                                AliasSuffixUiModel(
                                    suffix = ".some@suffix.test",
                                    signedSuffix = "",
                                    isCustom = false,
                                    domain = ""
                                )
                            ),
                            mailboxes = listOf(
                                AliasMailboxUiModel(
                                    id = 1,
                                    email = "some.mailbox@test.local"
                                )
                            )
                        ),
                        selectedSuffix = AliasSuffixUiModel(
                            suffix = ".some@suffix.test",
                            signedSuffix = "",
                            isCustom = false,
                            domain = ""
                        ),
                        mailboxes = listOf(
                            SelectedAliasMailboxUiModel(
                                model = AliasMailboxUiModel(
                                    id = 1,
                                    email = "some.mailbox@test.local"
                                ),
                                selected = true
                            ),
                            SelectedAliasMailboxUiModel(
                                model = AliasMailboxUiModel(
                                    id = 2,
                                    email = "other.mailbox@test.local"
                                ),
                                selected = false
                            ),
                        )
                    ),
                    isDraft = false,
                    errorList = emptySet(),
                    isLoadingState = IsLoadingState.NotLoading,
                    isAliasSavedState = AliasSavedState.Unknown,
                    isAliasDraftSavedState = AliasDraftSavedState.Unknown,
                    isApplyButtonEnabled = IsButtonEnabled.Enabled
                ),
                showAdvancedOptionsInitially = true,
                onCancel = {},
                onConfirm = {},
                onPrefixChanged = {},
                onSuffixChanged = {},
                onMailboxesChanged = {}
            )
        }
    }
}
