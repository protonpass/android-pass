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

package proton.android.pass.features.itemcreate.note

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.none
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.StickyTotpOptions
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldStickyFormOptionsContentType
import proton.android.pass.features.itemcreate.common.customfields.customFieldsList
import proton.android.pass.features.itemcreate.note.NoteContentUiEvent.OnAttachmentEvent

@Composable
internal fun NoteItemForm(
    modifier: Modifier = Modifier,
    noteItemFormState: NoteItemFormState,
    canUseCustomFields: Boolean,
    focusedField: Option<NoteField>,
    isFileAttachmentsEnabled: Boolean,
    isCustomItemEnabled: Boolean,
    displayFileAttachmentsOnboarding: Boolean,
    attachmentsState: AttachmentsState,
    enabled: Boolean,
    onTitleRequiredError: Boolean,
    customFieldValidationErrors: List<CustomFieldValidationError>,
    onEvent: (NoteContentUiEvent) -> Unit
) {
    Box(modifier = modifier) {
        val currentStickyFormOption = remember(focusedField) {
            when (val field = focusedField.value()) {
                is NoteField.CustomField -> when (field.field.type) {
                    CustomFieldType.Totp -> CustomFieldStickyFormOptionsContentType.AddTotp
                    else -> CustomFieldStickyFormOptionsContentType.NoOption
                }
                else -> CustomFieldStickyFormOptionsContentType.NoOption
            }
        }

        val isCurrentStickyVisible = currentStickyFormOption != CustomFieldStickyFormOptionsContentType.NoOption

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Spacing.medium)
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {

            AnimatedVisibility(isFileAttachmentsEnabled && displayFileAttachmentsOnboarding) {
                AttachmentBanner(Modifier.padding(bottom = Spacing.mediumSmall)) {
                    onEvent(NoteContentUiEvent.DismissAttachmentBanner)
                }
            }

            if (isCustomItemEnabled) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        TitleSection(
                            modifier = Modifier
                                .padding(bottom = Spacing.small)
                                .roundedContainerNorm()
                                .padding(
                                    start = Spacing.medium,
                                    top = Spacing.medium,
                                    end = Spacing.extraSmall,
                                    bottom = Spacing.medium
                                ),
                            value = noteItemFormState.title,
                            requestFocus = true,
                            onTitleRequiredError = onTitleRequiredError,
                            enabled = enabled,
                            isRounded = true,
                            onChange = { onEvent(NoteContentUiEvent.OnTitleChange(it)) }
                        )
                    }

                    item {
                        RoundedNoteSection(
                            modifier = Modifier.padding(vertical = Spacing.extraSmall),
                            textFieldModifier = Modifier.fillMaxWidth(),
                            enabled = enabled,
                            value = noteItemFormState.note,
                            onChange = { onEvent(NoteContentUiEvent.OnNoteChange(it)) }
                        )
                    }

                    customFieldsList(
                        modifier = Modifier.padding(vertical = Spacing.extraSmall),
                        customFields = noteItemFormState.customFields,
                        enabled = enabled,
                        errors = customFieldValidationErrors.toPersistentSet(),
                        isVisible = true,
                        canCreateCustomFields = canUseCustomFields,
                        sectionIndex = None,
                        focusedField = focusedField.flatMap {
                            (it as? NoteField.CustomField)?.field?.some() ?: none()
                        },
                        itemCategory = ItemCategory.Note,
                        onEvent = { onEvent(NoteContentUiEvent.OnCustomFieldEvent(it)) }
                    )

                    if (isFileAttachmentsEnabled) {
                        item {
                            AttachmentSection(
                                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                                attachmentsState = attachmentsState,
                                isDetail = false,
                                itemColors = passItemColors(ItemCategory.Note),
                                onEvent = { onEvent(OnAttachmentEvent(it)) }
                            )
                        }
                    }
                    if (isCurrentStickyVisible) {
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    NoteTitle(
                        modifier = Modifier.padding(bottom = Spacing.small),
                        value = noteItemFormState.title,
                        requestFocus = true,
                        onTitleRequiredError = onTitleRequiredError,
                        enabled = enabled,
                        onValueChanged = { onEvent(NoteContentUiEvent.OnTitleChange(it)) }
                    )
                    val shouldApplyNoteWeight =
                        remember(isFileAttachmentsEnabled, attachmentsState) {
                            !isFileAttachmentsEnabled || !attachmentsState.hasAnyAttachment
                        }
                    FullNoteSection(
                        modifier = Modifier
                            .applyIf(shouldApplyNoteWeight, ifTrue = { Modifier.weight(1f) }),
                        textFieldModifier = Modifier
                            .applyIf(shouldApplyNoteWeight, ifTrue = { Modifier.weight(1f) })
                            .fillMaxWidth(),
                        enabled = enabled,
                        value = noteItemFormState.note,
                        onChange = { onEvent(NoteContentUiEvent.OnNoteChange(it)) }
                    )

                    if (isFileAttachmentsEnabled) {
                        AttachmentList(
                            attachmentsState = attachmentsState,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding(),
            visible = isCurrentStickyVisible
        ) {
            when (currentStickyFormOption) {
                CustomFieldStickyFormOptionsContentType.AddTotp -> {
                    val context = LocalContext.current
                    val hasCamera = remember(LocalContext.current) {
                        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                    }

                    StickyTotpOptions(
                        hasCamera = hasCamera,
                        passItemColors = passItemColors(ItemCategory.Note),
                        onPasteCode = {
                            onEvent(NoteContentUiEvent.PasteTotp)
                        },
                        onScanCode = {
                            val focusedField = focusedField as? NoteField.CustomField
                            onEvent(NoteContentUiEvent.OnScanTotp(focusedField?.field?.index.toOption()))
                        }
                    )
                }

                CustomFieldStickyFormOptionsContentType.NoOption -> {}
            }
        }
    }
}
