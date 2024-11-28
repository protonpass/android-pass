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

package proton.android.pass.featureitemcreate.impl.common.attachments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.attachments.AttachmentRow
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun AttachmentSection(
    modifier: Modifier = Modifier,
    files: List<Attachment>,
    loadingFile: Option<Attachment>,
    onAttachmentOptions: (Attachment) -> Unit,
    onAttachmentClick: (Attachment) -> Unit,
    onAddAttachment: () -> Unit
) {
    Column(
        modifier = modifier
            .roundedContainerNorm()
            .padding(Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AttachmentHeader(isEnabled = loadingFile is None, fileAmount = files.size, onTrashAll = {})
        LazyColumn {
            itemsIndexed(files) { index, file ->
                AttachmentRow(
                    modifier = Modifier.padding(vertical = Spacing.small),
                    filename = file.name,
                    attachmentType = file.type,
                    size = file.size,
                    isEnabled = loadingFile is None,
                    isLoading = loadingFile.value()?.id == file.id,
                    onOptionsClick = { onAttachmentOptions(file) },
                    onAttachmentClick = { onAttachmentClick(file) }
                )
                if (index < files.lastIndex) {
                    PassDivider()
                }
            }
        }
        AddAttachmentButton(isEnabled = loadingFile is None, onClick = onAddAttachment)
    }
}

@Composable
fun AttachmentHeader(
    modifier: Modifier = Modifier,
    fileAmount: Int,
    isEnabled: Boolean,
    onTrashAll: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Icon.Default(
            id = CoreR.drawable.ic_proton_paper_clip,
            tint = PassTheme.colors.textWeak
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text.Body3Regular(stringResource(R.string.attachment_title))
            val text = if (fileAmount > 0) {
                pluralStringResource(R.plurals.attachment_file_amount, fileAmount, fileAmount)
            } else {
                stringResource(R.string.attachment_no_files)
            }
            Text.Body1Weak(text)
        }
        if (fileAmount > 0) {
            Button.CircleIcon(
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                enabled = isEnabled,
                iconId = CoreR.drawable.ic_proton_trash,
                iconTint = PassTheme.colors.loginInteractionNormMajor2,
                onClick = onTrashAll
            )
        }
    }
}

@Composable
fun AddAttachmentButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button.Circular(
        modifier = modifier.fillMaxWidth(),
        color = PassTheme.colors.loginInteractionNormMinor1,
        contentPadding = PaddingValues(Spacing.mediumSmall),
        enabled = isEnabled,
        onClick = onClick
    ) {
        val adjustedTextColor = if (isEnabled) {
            PassTheme.colors.loginInteractionNormMajor2
        } else {
            PassTheme.colors.loginInteractionNormMajor2.copy(
                alpha = 0.3f
            )
        }
        Text.Body1Regular(
            text = stringResource(R.string.attachment_add_file),
            color = adjustedTextColor
        )
    }
}

class ThemeAttachmentSectionPreviewProvider :
    ThemePairPreviewProvider<AttachmentSectionInput>(AttachmentSectionPreviewProvider())

@Preview
@Composable
fun AttachmentSectionPreview(
    @PreviewParameter(ThemeAttachmentSectionPreviewProvider::class) input: Pair<Boolean, AttachmentSectionInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AttachmentSection(
                files = input.second.files,
                loadingFile = input.second.loadingFile,
                onAttachmentOptions = {},
                onAttachmentClick = {},
                onAddAttachment = {}
            )
        }
    }
}
