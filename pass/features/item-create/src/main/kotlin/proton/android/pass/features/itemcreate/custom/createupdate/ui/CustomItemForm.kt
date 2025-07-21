/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.isCollapsedSaver
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.labels.CollapsibleSectionHeader
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.StickyTotpOptions
import proton.android.pass.features.itemcreate.common.customfields.AddSectionButton
import proton.android.pass.features.itemcreate.common.customfields.customFieldsList
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemSharedProperties
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemStaticFields
import proton.android.pass.features.itemcreate.custom.createupdate.ui.ItemContentEvent.OnOpenTOTPScanner
import proton.android.pass.features.itemcreate.custom.createupdate.ui.ItemContentEvent.OnPasteTOTPSecret

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CustomItemForm(
    modifier: Modifier,
    itemFormState: ItemFormState,
    itemSharedProperties: ItemSharedProperties,
    onEvent: (ItemContentEvent) -> Unit
) {
    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver<Int>()) {
        mutableStateListOf()
    }

    val isCurrentStickyVisible = remember(itemSharedProperties.focusedField) {
        itemSharedProperties.focusedField.value()?.type == CustomFieldType.Totp
    }

    val shouldShowAttachmentBanner = remember(
        key1 = itemSharedProperties.showFileAttachments,
        key2 = itemSharedProperties.showFileAttachmentsBanner
    ) { itemSharedProperties.showFileAttachments && itemSharedProperties.showFileAttachmentsBanner }

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = shouldShowAttachmentBanner) {
        if (shouldShowAttachmentBanner && lazyListState.firstVisibleItemIndex > 0) {
            scope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(state = lazyListState) {
            item {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium),
                    visible = shouldShowAttachmentBanner
                ) {
                    AttachmentBanner(Modifier.padding(vertical = Spacing.small)) {
                        onEvent(ItemContentEvent.DismissAttachmentBanner)
                    }
                }
            }
            item {
                TitleSection(
                    modifier = Modifier
                        .padding(horizontal = Spacing.medium)
                        .padding(vertical = Spacing.small)
                        .roundedContainerNorm()
                        .padding(
                            start = Spacing.medium,
                            top = Spacing.medium,
                            end = Spacing.extraSmall,
                            bottom = Spacing.medium
                        ),
                    value = itemFormState.title,
                    requestFocus = true,
                    onTitleRequiredError = itemSharedProperties.validationErrors.contains(
                        CommonFieldValidationError.BlankTitle
                    ),
                    enabled = itemSharedProperties.isFormEnabled,
                    isRounded = true,
                    onChange = {
                        onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.Title, it))
                    }
                )
            }

            when (itemFormState.itemStaticFields) {
                ItemStaticFields.Custom -> {}
                is ItemStaticFields.SSHKey -> item {
                    SSHKeyContent(
                        modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                        itemStaticFields = itemFormState.itemStaticFields,
                        isEditAllowed = itemSharedProperties.isFormEnabled,
                        onEvent = onEvent
                    )
                }

                is ItemStaticFields.WifiNetwork -> item {
                    WifiNetworkContent(
                        modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                        itemStaticFields = itemFormState.itemStaticFields,
                        isEditAllowed = itemSharedProperties.isFormEnabled,
                        onEvent = onEvent
                    )
                }
            }

            customFieldsList(
                modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                customFields = itemFormState.customFieldList,
                enabled = itemSharedProperties.isFormEnabled,
                errors = itemSharedProperties.validationErrors
                    .filterIsInstance<CustomFieldValidationError>()
                    .toPersistentSet(),
                isVisible = true,
                canCreateCustomFields = true,
                showLeadingIcon = false,
                sectionIndex = None,
                focusedField = itemSharedProperties.focusedField,
                itemCategory = ItemCategory.Custom,
                onEvent = { onEvent(ItemContentEvent.OnCustomFieldEvent(it)) }
            )

            itemFormState.sectionList.forEachIndexed { sectionIndex, section ->
                stickyHeader {
                    CollapsibleSectionHeader(
                        sectionTitle = section.title,
                        isCollapsed = isGroupCollapsed.contains(sectionIndex),
                        onClick = {
                            if (isGroupCollapsed.contains(sectionIndex)) {
                                isGroupCollapsed.remove(sectionIndex)
                            } else {
                                isGroupCollapsed.add(sectionIndex)
                            }
                        },
                        onOptionsClick = {
                            onEvent(ItemContentEvent.OnSectionOptions(sectionIndex, section.title))
                        }
                    )
                }

                customFieldsList(
                    modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                    customFields = section.customFields,
                    enabled = itemSharedProperties.isFormEnabled,
                    errors = itemSharedProperties.validationErrors
                        .filterIsInstance<CustomFieldValidationError>()
                        .toPersistentSet(),
                    isVisible = !isGroupCollapsed.contains(sectionIndex),
                    canCreateCustomFields = true,
                    showLeadingIcon = false,
                    sectionIndex = sectionIndex.some(),
                    focusedField = itemSharedProperties.focusedField,
                    itemCategory = ItemCategory.Custom,
                    onEvent = { onEvent(ItemContentEvent.OnCustomFieldEvent(it)) }
                )
            }
            item {
                PassDivider(
                    modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall)
                )
                AddSectionButton(
                    modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                    isEnabled = itemSharedProperties.isFormEnabled,
                    passItemColors = passItemColors(ItemCategory.Custom),
                    onClick = { onEvent(ItemContentEvent.OnAddSection) }
                )
            }
            item {
                PassDivider(
                    modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall)
                )
                SimpleNoteSection(
                    modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                    value = itemFormState.note,
                    enabled = itemSharedProperties.isFormEnabled,
                    onChange = { onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.Note, it)) }
                )
            }
            if (itemSharedProperties.showFileAttachments) {
                item {
                    AttachmentSection(
                        modifier = Modifier.padding(horizontal = Spacing.medium).padding(vertical = Spacing.extraSmall),
                        attachmentsState = itemSharedProperties.attachmentsState,
                        isDetail = false,
                        itemColors = passItemColors(ItemCategory.Custom),
                        onEvent = { onEvent(ItemContentEvent.OnAttachmentEvent(it)) }
                    )
                }
            }
            if (isCurrentStickyVisible) {
                item { Spacer(modifier = Modifier.height(48.dp)) }
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
            val hasCamera = remember(context) {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            }

            StickyTotpOptions(
                hasCamera = hasCamera,
                passItemColors = passItemColors(ItemCategory.Custom),
                onPasteCode = { onEvent(OnPasteTOTPSecret) },
                onScanCode = {
                    itemSharedProperties.focusedField.value()?.let {
                        onEvent(OnOpenTOTPScanner(it.sectionIndex, it.index))
                    }
                }
            )
        }
    }
}
