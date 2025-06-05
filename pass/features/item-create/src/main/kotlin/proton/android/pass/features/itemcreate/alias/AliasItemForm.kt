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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.None
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.SenderNameSection
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.AliasContentUiEvent.OnAttachmentEvent
import proton.android.pass.features.itemcreate.alias.banner.AliasAdvancedOptionsBanner
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.customfields.customFieldsList
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun AliasItemForm(
    modifier: Modifier = Modifier,
    aliasItemFormState: AliasItemFormState,
    isCreateMode: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    isAliasCreatedByUser: Boolean,
    showUpgrade: Boolean,
    displayFileAttachmentsOnboarding: Boolean,
    displayAdvancedOptionsBanner: Boolean,
    isFileAttachmentsEnabled: Boolean,
    isCustomTypeEnabled: Boolean,
    attachmentsState: AttachmentsState,
    canUseCustomFields: Boolean,
    focusedField: AliasField?,
    customFieldValidationErrors: ImmutableList<CustomFieldValidationError>,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit,
    onEvent: (AliasContentUiEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.medium)
    ) {
        item {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = showUpgrade
            ) {
                InfoBanner(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall),
                    backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                    text = stringResource(R.string.create_alias_content_limit_banner)
                )
            }
        }

        item {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = isFileAttachmentsEnabled && displayFileAttachmentsOnboarding
            ) {
                AttachmentBanner(
                    modifier = Modifier.padding(vertical = Spacing.small)
                ) {
                    onEvent(AliasContentUiEvent.DismissAttachmentBanner)
                }
            }
        }

        item {
            TitleSection(
                modifier = Modifier
                    .padding(vertical = Spacing.small)
                    .roundedContainerNorm()
                    .padding(
                        start = Spacing.medium,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = aliasItemFormState.title,
                requestFocus = !isLoading,
                onTitleRequiredError = onTitleRequiredError,
                enabled = isEditAllowed,
                isRounded = true,
                onChange = { onEvent(AliasContentUiEvent.OnTitleChange(it)) }
            )
        }

        item {
            if (isCreateMode) {
                CreateAliasSection(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall),
                    state = aliasItemFormState,
                    onChange = { onEvent(AliasContentUiEvent.OnPrefixChange(it)) },
                    onSuffixClick = onSuffixClick,
                    canEdit = isEditAllowed,
                    canSelectSuffix = aliasItemFormState.aliasOptions.suffixes.size > 1,
                    onAdvancedOptionsClicked = { onEvent(AliasContentUiEvent.DismissAdvancedOptionsBanner) },
                    onAliasRequiredError = onAliasRequiredError,
                    onInvalidAliasError = onInvalidAliasError
                )
            } else {
                DisplayAliasSection(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall),
                    state = aliasItemFormState,
                    isLoading = isLoading
                )
            }
        }
        item {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = displayAdvancedOptionsBanner
            ) {
                AliasAdvancedOptionsBanner(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall)
                ) {
                    onEvent(AliasContentUiEvent.DismissAdvancedOptionsBanner)
                }
            }
        }

        item {
            MailboxSection(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                isBottomSheet = false,
                selectedMailboxes = aliasItemFormState.selectedMailboxes.toPersistentList(),
                isCreateMode = isCreateMode,
                isEditAllowed = isEditAllowed && aliasItemFormState.aliasOptions.mailboxes.size > 1,
                isLoading = isLoading,
                onMailboxClick = onMailboxClick
            )
        }

        item {
            SimpleNoteSection(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                value = aliasItemFormState.note,
                enabled = isEditAllowed,
                onChange = { onEvent(AliasContentUiEvent.OnNoteChange(it)) }
            )
        }

        aliasItemFormState.slNote?.let { slNote ->
            item {
                SimpleNoteSection(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall),
                    label = buildString {
                        append(stringResource(id = CompR.string.field_note_title))
                        append(" ${SpecialCharacters.DOT_SEPARATOR} ")
                        append(stringResource(id = CompR.string.simple_login_brand_name))
                    },
                    labelIcon = {
                        Icon(
                            modifier = Modifier
                                .clickable { onEvent(AliasContentUiEvent.OnSlNoteInfoClick) }
                                .size(size = 16.dp),
                            painter = painterResource(CoreR.drawable.ic_proton_question_circle),
                            contentDescription = stringResource(id = R.string.sl_note_info_content_description),
                            tint = PassTheme.colors.textWeak
                        )
                    },
                    value = slNote,
                    enabled = isEditAllowed,
                    onChange = { onEvent(AliasContentUiEvent.OnSLNoteChange(it)) }
                )
            }
        }
        if (isCreateMode || isAliasCreatedByUser) {
            item {
                SenderNameSection(
                    modifier = Modifier.padding(vertical = Spacing.extraSmall),
                    value = aliasItemFormState.senderName.orEmpty(),
                    enabled = isEditAllowed,
                    onChange = { onEvent(AliasContentUiEvent.OnSenderNameChange(it)) }
                )
            }
        }

        if (isCustomTypeEnabled) {
            customFieldsList(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                customFields = aliasItemFormState.customFields,
                enabled = isEditAllowed,
                errors = customFieldValidationErrors.toPersistentSet(),
                isVisible = true,
                canCreateCustomFields = canUseCustomFields,
                sectionIndex = None,
                focusedField = (focusedField as? AliasField.CustomField)?.field.toOption(),
                itemCategory = ItemCategory.Alias,
                onEvent = { onEvent(AliasContentUiEvent.OnCustomFieldEvent(it)) }
            )
        }

        if (isFileAttachmentsEnabled) {
            item {
                AttachmentSection(
                    attachmentsState = attachmentsState,
                    isDetail = false,
                    itemColors = passItemColors(ItemCategory.Alias),
                    onEvent = { onEvent(OnAttachmentEvent(it)) }
                )
            }
        }
    }
}
