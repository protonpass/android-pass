/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.custom

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR

private const val HIDDEN_PRIVATE_KEY_TEXT_LENGTH = 12

@Composable
internal fun PassSSHKeyMainSection(
    modifier: Modifier = Modifier,
    contents: ItemContents.SSHKey,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.SSHKey,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    RoundedCornersColumn(modifier = modifier) {
        PassItemDetailFieldRow(
            icon = CoreR.drawable.ic_proton_text_align_left,
            title = stringResource(R.string.item_details_ssh_key_label_public_key),
            subtitle = contents.publicKey,
            itemColors = itemColors,
            itemDiffType = itemDiffs.publicKey,
            onClick = {
                onEvent(
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = contents.publicKey,
                        field = ItemDetailsFieldType.Plain.PublicKey
                    )
                )
            }
        )

        PassDivider()

        PassItemDetailsHiddenFieldRow(
            icon = CoreR.drawable.ic_proton_key,
            title = stringResource(R.string.item_details_ssh_key_label_private_key),
            hiddenState = contents.privateKey,
            hiddenTextLength = HIDDEN_PRIVATE_KEY_TEXT_LENGTH,
            itemColors = itemColors,
            itemDiffType = itemDiffs.privateKey,
            hiddenTextStyle = ProtonTheme.typography.defaultNorm
                .copy(fontFamily = FontFamily.Monospace),
            onClick = {
                onEvent(
                    PassItemDetailsUiEvent.OnHiddenFieldClick(
                        state = contents.privateKey,
                        field = ItemDetailsFieldType.Hidden.PrivateKey
                    )
                )
            },
            onToggle = { isVisible ->
                onEvent(
                    PassItemDetailsUiEvent.OnHiddenFieldToggle(
                        isVisible = isVisible,
                        hiddenState = contents.privateKey,
                        fieldType = ItemDetailsFieldType.Hidden.PrivateKey,
                        fieldSection = ItemSection.SSHKey
                    )
                )
            }
        )
    }
}

@Preview
@Composable
internal fun PassSSHKeyMainSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassSSHKeyMainSection(
                contents = ItemContents.SSHKey(
                    publicKey = "Public key",
                    privateKey = HiddenState.Empty(""),
                    note = "",
                    title = "",
                    customFieldList = emptyList(),
                    sectionContentList = emptyList()
                ),
                itemColors = passItemColors(ItemCategory.SSHKey),
                itemDiffs = ItemDiffs.SSHKey(),
                onEvent = {}
            )
        }
    }
}
