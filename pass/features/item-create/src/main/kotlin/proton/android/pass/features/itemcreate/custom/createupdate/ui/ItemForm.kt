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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.isCollapsedSaver
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.labels.CollapsibleSectionHeader
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemSharedProperties

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemForm(
    modifier: Modifier,
    itemFormState: ItemFormState,
    itemSharedProperties: ItemSharedProperties,
    onEvent: (ItemContentEvent) -> Unit
) {
    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver<Int>()) {
        mutableStateListOf()
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = itemSharedProperties.showFileAttachments &&
                    itemSharedProperties.showFileAttachmentsBanner
            ) {
                AttachmentBanner(
                    modifier = Modifier
                        .padding(horizontal = Spacing.medium)
                        .padding(bottom = Spacing.mediumSmall)
                ) {
                    onEvent(ItemContentEvent.DismissAttachmentBanner)
                }
            }
        }
        item {
            TitleSection(
                modifier = Modifier
                    .padding(vertical = Spacing.small)
                    .padding(horizontal = Spacing.medium)
                    .roundedContainerNorm()
                    .padding(
                        start = Spacing.medium,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = itemFormState.title,
                requestFocus = true,
                onTitleRequiredError = false,
                enabled = itemSharedProperties.isFormEnabled,
                isRounded = true,
                onChange = { onEvent(ItemContentEvent.OnTitleChange(it)) }
            )
        }

        customFieldsList(
            customFields = itemFormState.customFieldList,
            enabled = itemSharedProperties.isFormEnabled,
            isVisible = true,
            sectionIndex = None,
            focusedField = itemSharedProperties.focusedField,
            onEvent = onEvent
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
                customFields = section.customFields,
                enabled = itemSharedProperties.isFormEnabled,
                isVisible = !isGroupCollapsed.contains(sectionIndex),
                sectionIndex = sectionIndex.some(),
                focusedField = itemSharedProperties.focusedField,
                onEvent = onEvent
            )
        }
        if (itemSharedProperties.canUseCustomFields) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    PassDivider()
                    AddSectionButton(
                        modifier = Modifier.fillMaxWidth(),
                        isEnabled = itemSharedProperties.isFormEnabled,
                        onClick = { onEvent(ItemContentEvent.OnAddSection) }
                    )
                }
            }
        }
        if (itemSharedProperties.showFileAttachments) {
            item {
                AttachmentSection(
                    modifier = Modifier
                        .padding(vertical = Spacing.small)
                        .padding(horizontal = Spacing.medium),
                    attachmentsState = itemSharedProperties.attachmentsState,
                    isDetail = false,
                    itemColors = passItemColors(ItemCategory.Custom),
                    itemDiffs = ItemDiffs.None,
                    onEvent = { onEvent(ItemContentEvent.OnAttachmentEvent(it)) }
                )
            }
        }
    }
}
