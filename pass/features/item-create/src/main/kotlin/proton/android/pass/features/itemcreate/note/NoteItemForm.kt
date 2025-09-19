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
import proton.android.pass.features.itemcreate.common.customfields.customFieldsList
import proton.android.pass.features.itemcreate.note.NoteContentUiEvent.OnAttachmentEvent

@Composable
internal fun NoteItemForm(
    modifier: Modifier = Modifier,
    noteItemFormState: NoteItemFormState,
    canUseCustomFields: Boolean,
    focusedField: Option<NoteField>,
    displayFileAttachmentsOnboarding: Boolean,
    attachmentsState: AttachmentsState,
    enabled: Boolean,
    onTitleRequiredError: Boolean,
    customFieldValidationErrors: List<CustomFieldValidationError>,
    onEvent: (NoteContentUiEvent) -> Unit
) {
    Box(modifier = modifier) {
        val isCurrentStickyVisible = remember(focusedField) {
            (focusedField.value() as? NoteField.CustomField)?.field?.type == CustomFieldType.Totp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {

            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = displayFileAttachmentsOnboarding
            ) {
                AttachmentBanner(modifier = Modifier.padding(vertical = Spacing.small)) {
                    onEvent(NoteContentUiEvent.DismissAttachmentBanner)
                }
            }

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

                item {
                    AttachmentSection(
                        modifier = Modifier.padding(vertical = Spacing.extraSmall),
                        attachmentsState = attachmentsState,
                        isDetail = false,
                        itemColors = passItemColors(ItemCategory.Note),
                        onEvent = { onEvent(OnAttachmentEvent(it)) }
                    )
                }
                if (isCurrentStickyVisible) {
                    item { Spacer(modifier = Modifier.height(48.dp)) }
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
                    val focusedField = focusedField.value() as? NoteField.CustomField
                    onEvent(NoteContentUiEvent.OnScanTotp(focusedField?.field?.index.toOption()))
                }
            )
        }
    }
}
